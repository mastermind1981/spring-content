package internal.org.springframework.content.mongo.boot.autoconfigure;

import com.mongodb.Mongo;

import internal.org.springframework.content.mongo.config.MongoStoreConfiguration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(Mongo.class)
@Import({MongoContentAutoConfigureRegistrar.class, MongoStoreConfiguration.class})
public class MongoContentAutoConfiguration {

}
