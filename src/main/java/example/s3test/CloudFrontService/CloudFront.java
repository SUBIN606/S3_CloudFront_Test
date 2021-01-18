package example.s3test.CloudFrontService;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.amazonaws.services.s3.internal.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@RequiredArgsConstructor
@Component
public class CloudFront {

    @Value("${cloud.aws.cloudFront.distributionDomain}")
    private String distributionDomain;

    @Value("${cloud.aws.cloudFront.keyPairId}")
    private String keyPairId;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.path}")
    private String path;

    public String getSignedURLWithCannedPolicy( String fileName ) throws InvalidKeySpecException,
                                                                         IOException {
        String signedURL = "";

        try {
            SignerUtils.Protocol protocol = SignerUtils.Protocol.https;
            File privateKeyFile = ResourceUtils.getFile(path);
            String s3ObjectKey = fileName;
            Date dateLessThan = getDateLessThan("second", 15);

            signedURL = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
                    protocol,
                    distributionDomain,
                    privateKeyFile,
                    s3ObjectKey,
                    keyPairId,
                    dateLessThan
            );

            log.info("********** signedURLWithCannedPolicy = " + signedURL);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return signedURL;
    }

    public String getSignedURLWithCustomPolicy (String fileName) throws InvalidKeySpecException,
                                                                        IOException {
        String signedURL = "";

        try {

            String resourcePath = "https://" + distributionDomain + "/" + fileName;
            Date dateLessThan = getDateLessThan("year", 1);
          //  String ipRange = "52.78.113.2";
            String ipRange = "0.0.0.0/0";

            String customPolicyForSignedUrl = CloudFrontUrlSigner.buildCustomPolicyForSignedUrl(
                    resourcePath, dateLessThan, ipRange, null
            );
            log.info("********** custom policy for signed url = " + customPolicyForSignedUrl);

            File privateKeyFile = ResourceUtils.getFile(path);
            PrivateKey privateKey = SignerUtils.loadPrivateKey(privateKeyFile);

            signedURL = CloudFrontUrlSigner.getSignedURLWithCustomPolicy(
                    resourcePath,
                    keyPairId,
                    privateKey,
                    customPolicyForSignedUrl
            );
            log.info("********** signedURLWithCustomPolicy = " + signedURL);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return signedURL;
    }

    private Date getDateLessThan (String field, int amount) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if(field.equals("year")) {
            calendar.add(Calendar.YEAR, amount);
        }else if(field.equals("month")) {
            calendar.add(Calendar.MONTH, amount);
        }else if(field.equals("second")) {
            calendar.add(Calendar.SECOND, amount);
        }else {
            calendar.add(Calendar.YEAR, 1);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(calendar.getTime());

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timeStr = timeFormat.format(calendar.getTime());
        log.info(dateStr + "T" + timeStr + ".00Z");
        Date dateLessThan = ServiceUtils.parseIso8601Date(dateStr + "T" + timeStr + ".00Z");

        return dateLessThan;
    }

}
