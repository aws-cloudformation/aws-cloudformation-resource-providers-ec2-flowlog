package software.amazon.ec2.flowlog;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ec2-flowlog.json");
    }

    /**
     * Overriding Configuration#resourceDefinedTags so that ResourceReqeustHandler#getDesiredResourceTags
     * would return both resource- and stack-level tags.
     * @param resourceModel
     * @return
     */
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        } else {
            return resourceModel.getTags().stream().collect(
                    Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue())
            );
        }
    }
}
