package internal.org.springframework.content.fs.config;

import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.AfterEach;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.BeforeEach;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Context;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Describe;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.FIt;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.It;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.content.commons.annotations.Content;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.content.fs.config.EnableFilesystemContentRepositories;
import org.springframework.content.fs.config.EnableFilesystemStores;
import org.springframework.content.fs.config.FilesystemStoreConverter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;

import com.github.paulcwarren.ginkgo4j.Ginkgo4jConfiguration;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;

@SuppressWarnings("deprecation")
@RunWith(Ginkgo4jRunner.class)
@Ginkgo4jConfiguration(threads=1)
public class EnableFilesystemStoresTest {

	private AnnotationConfigApplicationContext context;
	{
		Describe("EnableFilesystemStores", () -> {

			Context("given a context and a configuartion with a filesystem content repository bean", () -> {
				BeforeEach(() -> {
					context = new AnnotationConfigApplicationContext();
					context.register(TestConfig.class);
					context.refresh();
				});
				AfterEach(() -> {
					context.close();
				});
				FIt("should have a ContentRepository bean", () -> {
					assertThat(context.getBean(TestEntityContentRepository.class), is(not(nullValue())));
				});
				It("should have a filesystem conversion service bean", () -> {
					assertThat(context.getBean("filesystemStoreConverter"), is(not(nullValue())));
				});
				It("should have a FilesystemProperties bean", () -> {
					assertThat(context.getBean(FilesystemProperties.class), is(not(nullValue())));
					assertThat(context.getBean(FilesystemProperties.class).getFilesystemRoot(), endsWith("/a/b/c/"));
				});
				It("should have a FileSystemResourceLoader bean", () -> {
					assertThat(context.getBean("fileSystemResourceLoader"), is(not(nullValue())));
				});
			});

			Context("given a context with a custom converter", () -> {
				BeforeEach(() -> {
					context = new AnnotationConfigApplicationContext();
					context.register(ConverterConfig.class);
					context.refresh();
				});
				AfterEach(() -> {
					context.close();
				});
				It("should use that converter", () -> {
					ConversionService converters = (ConversionService) context.getBean("filesystemStoreConverter");
					assertThat(converters.convert(UUID.fromString("e49d5464-26ce-11e7-93ae-92361f002671"), String.class), is("/e49d5464/26ce/11e7/93ae/92361f002671"));
				});
			});
			
			Context("given a context with an empty configuration", () -> {
				BeforeEach(() -> {
					context = new AnnotationConfigApplicationContext();
					context.register(EmptyConfig.class);
					context.refresh();
				});
				AfterEach(() -> {
					context.close();
				});
				It("should not contain any filesystem repository beans", () -> {
					try {
						context.getBean(TestEntityContentRepository.class);
						fail("expected no such bean");
					} catch (NoSuchBeanDefinitionException e) {
						assertThat(true, is(true));
					}
				});
			});
		});
		
		Describe("EnableFilesystemContentRepositories", () -> {

			Context("given a context and a configuartion with a filesystem content repository bean", () -> {
				BeforeEach(() -> {
					context = new AnnotationConfigApplicationContext();
					context.register(BackwardCompatibilityConfig.class);
					context.refresh();
				});
				AfterEach(() -> {
					context.close();
				});
				It("should have a ContentRepository bean", () -> {
					assertThat(context.getBean(TestEntityContentRepository.class), is(not(nullValue())));
				});
			});
		});

	}


	@Test
	public void noop() {
	}

	@Configuration
	@EnableFilesystemStores(basePackages="contains.no.fs.repositories")
    @PropertySource("classpath:/test.properties")
	public static class EmptyConfig {
	}

	@Configuration
	@EnableFilesystemStores
	@PropertySource("classpath:/test.properties")
	public static class TestConfig {
	}

	@Configuration
	@EnableFilesystemStores
	@PropertySource("classpath:/test.properties")
	public static class ConverterConfig {
		@Bean
		public FilesystemStoreConverter<UUID,String> uuidConverter() {
			return new FilesystemStoreConverter<UUID,String>() {

				@Override
				public String convert(UUID source) {
					return String.format("/%s", source.toString().replaceAll("-","/"));
				}
				
			};
		}
	}

	@EnableFilesystemContentRepositories
	@PropertySource("classpath:/test.properties")
	public static class BackwardCompatibilityConfig {
	}

	@Content
	public class TestEntity {
		@ContentId
		private String contentId;
	}

	public interface TestEntityContentRepository extends ContentStore<TestEntity, String> {
	}
}
