package it.bandimonitor.domain.ckan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CkanPackage {
    private String id;
    private String name;
    private String title;
    private String notes;
    private String url;
    private String metadata_created;
    private String metadata_modified;
    private CkanOrganization organization;
    private List<CkanTag> tags;
    private List<CkanExtra> extras;
}
