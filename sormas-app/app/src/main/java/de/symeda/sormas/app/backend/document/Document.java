package de.symeda.sormas.app.backend.document;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.user.User;

@DatabaseTable(tableName = "documents")
public class Document extends AbstractDomainObject {

    public static final String TABLE_NAME = "documents";

    public static final String UPLOADING_USER = "uploadingUser";
    public static final String RELATED_ENTITY_UUID = "relatedEntityUuid";
    public static final String RELATED_ENTITY_TYPE = "relatedEntityType";
    public static final String NAME = "name";
    public static final String MIME_TYPE = "mimeType";
    public static final String SIZE = "size";
    public static final String CONTENT = "content";

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private User uploadingUser;

    @DatabaseField
    private String relatedEntityUuid;

    @DatabaseField
    private String relatedEntityType;      // "CAMPAIGN_FORM_DATA"

    @DatabaseField
    private String name;

    @DatabaseField
    private String mimeType;

    @DatabaseField
    private long size;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] content;

    public User getUploadingUser() {
        return uploadingUser;
    }

    public void setUploadingUser(User uploadingUser) {
        this.uploadingUser = uploadingUser;
    }

    public String getRelatedEntityUuid() {
        return relatedEntityUuid;
    }

    public void setRelatedEntityUuid(String relatedEntityUuid) {
        this.relatedEntityUuid = relatedEntityUuid;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
