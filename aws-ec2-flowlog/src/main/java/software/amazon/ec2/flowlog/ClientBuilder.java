package software.amazon.ec2.flowlog;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.cloudformation.LambdaWrapper;

class ClientBuilder {
    static Ec2Client getClient() {
        return Ec2Client.builder()
            .credentialsProvider(
                    new AwsCredentialsProvider() {
                        @Override
                        public AwsCredentials resolveCredentials() {
                            throw new RuntimeException("Must not use default client credential");
                        }
                    })
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
    }
}
