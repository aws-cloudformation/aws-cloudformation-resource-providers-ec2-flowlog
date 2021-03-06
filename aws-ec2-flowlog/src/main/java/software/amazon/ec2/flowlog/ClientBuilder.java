package software.amazon.ec2.flowlog;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.cloudformation.LambdaWrapper;

class ClientBuilder {
    static Ec2Client getClient() {
        return Ec2Client.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
    }
}
