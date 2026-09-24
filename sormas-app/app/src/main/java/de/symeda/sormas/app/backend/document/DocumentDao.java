package de.symeda.sormas.app.backend.document;

import com.j256.ormlite.dao.Dao;

import de.symeda.sormas.app.backend.common.AbstractAdoDao;

public class DocumentDao extends AbstractAdoDao<Document> {

    public DocumentDao(Dao<Document, Long> innerDao) {
        super(innerDao);
    }

    @Override
    protected Class<Document> getAdoClass() {
        return Document.class;
    }

    @Override
    public String getTableName() {
        return Document.TABLE_NAME;
    }
}
