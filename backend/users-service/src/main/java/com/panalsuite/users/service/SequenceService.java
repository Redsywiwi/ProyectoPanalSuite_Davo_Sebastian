package com.panalsuite.users.service;

import com.panalsuite.users.model.Counter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

// Genera ids numericos autoincrementales (Long) para que coincidan con los modelos de la app.
@Service
public class SequenceService {
    private final MongoOperations mongo;

    public SequenceService(MongoOperations mongo) {
        this.mongo = mongo;
    }

    public long next(String name) {
        Counter counter = mongo.findAndModify(
                Query.query(Criteria.where("_id").is(name)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Counter.class);
        return counter.getSeq();
    }

    // Usado por el seeder: deja el contador en al menos "value" sin bajarlo nunca
    public void ensureAtLeast(String name, long value) {
        mongo.findAndModify(
                Query.query(Criteria.where("_id").is(name)),
                new Update().max("seq", value),
                FindAndModifyOptions.options().upsert(true),
                Counter.class);
    }
}
