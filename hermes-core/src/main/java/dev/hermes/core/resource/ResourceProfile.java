package dev.hermes.core.resource;

/** Parsed {@code resources/profile.json} document (version 1). */
public final class ResourceProfile {

    private final int version;
    private final String catalog;
    private final String bundlesDirectory;
    private final boolean defaultAsync;
    private final boolean htmlDefaultAsync;
    private final int cooperativeAssetsPerFrame;
    private final boolean showLoadingScreenWhenAsync;

    public ResourceProfile(
            int version,
            String catalog,
            String bundlesDirectory,
            boolean defaultAsync,
            boolean htmlDefaultAsync,
            int cooperativeAssetsPerFrame,
            boolean showLoadingScreenWhenAsync) {
        this.version = version;
        this.catalog = catalog;
        this.bundlesDirectory = bundlesDirectory;
        this.defaultAsync = defaultAsync;
        this.htmlDefaultAsync = htmlDefaultAsync;
        this.cooperativeAssetsPerFrame = cooperativeAssetsPerFrame;
        this.showLoadingScreenWhenAsync = showLoadingScreenWhenAsync;
    }

    public static ResourceProfile defaults() {
        return new ResourceProfile(1, "resources/catalog.json", "resources/bundles", false, true, 1, true);
    }

    public int version() {
        return version;
    }

    public String catalog() {
        return catalog;
    }

    public String bundlesDirectory() {
        return bundlesDirectory;
    }

    public boolean defaultAsync() {
        return defaultAsync;
    }

    public boolean htmlDefaultAsync() {
        return htmlDefaultAsync;
    }

    public int cooperativeAssetsPerFrame() {
        return cooperativeAssetsPerFrame;
    }

    public boolean showLoadingScreenWhenAsync() {
        return showLoadingScreenWhenAsync;
    }
}
