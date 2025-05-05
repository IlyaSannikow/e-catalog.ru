package pro.akosarev.sandbox.entity;

public class RateLimitProperties {
    private RateLimitConfig global = new RateLimitConfig();
    private RateLimitConfig publicAccess = new RateLimitConfig();
    private RateLimitConfig protectedAccess = new RateLimitConfig();
    private RateLimitConfig critical = new RateLimitConfig();
    private RateLimitEndpointConfig endpoints = new RateLimitEndpointConfig();
    public RateLimitConfig getGlobal() {
        return global;
    }

    public RateLimitConfig getPublic() {
        return publicAccess;
    }

    public void setPublic(RateLimitConfig publicAccess) {
        this.publicAccess = publicAccess;
    }

    public RateLimitConfig getProtected() {
        return protectedAccess;
    }

    public void setProtected(RateLimitConfig protectedAccess) {
        this.protectedAccess = protectedAccess;
    }

    public RateLimitConfig getCritical() {
        return critical;
    }

    public void setCritical(RateLimitConfig critical) {
        this.critical = critical;
    }

    public RateLimitEndpointConfig getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(RateLimitEndpointConfig endpoints) {
        this.endpoints = endpoints;
    }
}
