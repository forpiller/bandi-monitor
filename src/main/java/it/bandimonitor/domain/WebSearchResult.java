package it.bandimonitor.domain;

import java.util.List;

public class WebSearchResult {

    private String summary;
    private List<SearchLink> links;
    private boolean success;
    private String errorMessage;

    public WebSearchResult() {}

    public WebSearchResult(String summary, List<SearchLink> links, boolean success, String errorMessage) {
        this.summary = summary;
        this.links = links;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<SearchLink> getLinks() { return links; }
    public void setLinks(List<SearchLink> links) { this.links = links; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public record SearchLink(String title, String url, String snippet, String engine) {}
}
