package nulldereference;

public class ResourceManager {

    /**
     * With this example, the resource should be checked for nullness.
     *
     * @param resource the resource to check
     * @return true if the resource is empty
     */
    public boolean simpleNullDeref(Resource resource){
        return resource.isEmpty();
    }

    /**
     * The resource must be checked for nullness first even if the explicit check
     * is the last condition.
     *
     * @param resource the resource to check
     * @return true if the resource is empty. Resource is not null
     */
    public boolean complexNullDeref(Resource resource){
        return resource.isEmpty();
    }
}
