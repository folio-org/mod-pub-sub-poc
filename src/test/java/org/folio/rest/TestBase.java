package org.folio.rest;

import static io.restassured.RestAssured.given;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.folio.okapi.common.XOkapiHeaders;
import org.folio.rest.client.TenantClient;
import org.folio.rest.tools.utils.NetworkUtils;
import org.folio.test.junit.TestStartLoggingRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Header;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Base test class for tests that use wiremock and vertx http servers,
 * test that inherits this class must use VertxUnitRunner as test runner
 */
public class TestBase {

  public static final String STUB_TENANT = "diku";

  protected static final Header JSON_CONTENT_TYPE_HEADER = new Header(HttpHeaders.CONTENT_TYPE,
    ContentType.APPLICATION_JSON.getMimeType());

  private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

  private static final String STUB_TOKEN = "TEST_OKAPI_TOKEN";
  private static final String host = "http://127.0.0.1";
  private static final String HTTP_PORT = "http.port";
  private static final int port = NetworkUtils.nextFreePort();

  protected static Vertx vertx;

  @Rule
  public TestRule watcher = TestStartLoggingRule.instance();

  @Rule
  public WireMockRule userMockServer = new WireMockRule(
    WireMockConfiguration.wireMockConfig()
      .dynamicPort()
      .notifier(new Slf4jNotifier(true)));

  @BeforeClass
  public static void setUpBeforeClass() {

    vertx = Vertx.vertx();

    Locale.setDefault(Locale.US);  // enforce English error messages

//    DeploymentOptions options = new DeploymentOptions().setInstances(3).setConfig(new JsonObject().put(HTTP_PORT, port));
    DeploymentOptions options = new DeploymentOptions().setConfig(
      new JsonObject()
        .put(HTTP_PORT, port)
        .put("spring.configuration", "org.folio.spring.config.TestConfig")
    );

    RestAssured.port = port;

    startVerticle(options);
  }

  private static void startVerticle(DeploymentOptions options) {

    logger.info("Start verticle");

    CompletableFuture<Void> future = new CompletableFuture<>();
    vertx.deployVerticle(RestVerticle.class.getName(), options, event -> {
      try {
        TenantClient tenantClient = new TenantClient(host + ":" + port, STUB_TENANT, STUB_TOKEN);
        tenantClient.postTenant(null, res2 -> future.complete(null));
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    });
    future.join();

    // Simple GET request to see the module is running and we can talk to it.
    given()
      .request()
      .get("/admin/health")
      .then()
      .log().all()
      .statusCode(200);
    logger.info("test: setup done. Using port " + port);
  }

  protected RequestSpecification getRequestSpecification() {
    return new RequestSpecBuilder()
      .addHeader(XOkapiHeaders.TENANT, STUB_TENANT)
      .addHeader(XOkapiHeaders.TOKEN, STUB_TOKEN)
      .addHeader(XOkapiHeaders.URL, getWiremockUrl())
      .setBaseUri(host + ":" + port)
      .setPort(port)
      .log(LogDetail.ALL)
      .build();
  }

  protected RequestSpecification givenWithUrl() {
    return new RequestSpecBuilder()
      .addHeader(XOkapiHeaders.URL, getWiremockUrl())
      .setBaseUri(host + ":" + port)
      .setPort(port)
      .log(LogDetail.ALL)
      .build();
  }

  /**
   * Returns url of Wiremock server used in this test
   */
  protected String getWiremockUrl() {
    return host + ":" + userMockServer.port();
  }

  protected ExtractableResponse<Response> getWithOk(String resourcePath) {
    return getWithStatus(resourcePath, HttpStatus.SC_OK);
  }

  protected ExtractableResponse<Response> deleteWithOk(String resourcePath) {
    return deleteWithStatus(resourcePath, HttpStatus.SC_NO_CONTENT);
  }

  protected ExtractableResponse<Response> putWithOk(String resourcePath, String putBody, Header updater) {
    return putWithStatus(resourcePath, putBody, HttpStatus.SC_NO_CONTENT, updater);
  }

  protected ExtractableResponse<Response> getWithStatus(String resourcePath, int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .when()
      .get(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus).extract();
  }

  protected ValidatableResponse getWithValidateBody(String resourcePath, int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .when()
      .get(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus);
  }

  protected ExtractableResponse<Response> putWithStatus(String resourcePath, String putBody,
                                                        int expectedStatus, Header userHeader) {
    return given()
      .spec(getRequestSpecification())
      .header(JSON_CONTENT_TYPE_HEADER)
      .header(userHeader)
      .body(putBody)
      .when()
      .put(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus)
      .extract();
  }

  protected ExtractableResponse<Response> postWithStatus(String resourcePath, String postBody,
                                                         int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .header(JSON_CONTENT_TYPE_HEADER)
      .body(postBody)
      .when()
      .post(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus)
      .extract();
  }

  protected ExtractableResponse<Response> deleteWithStatus(String resourcePath, int expectedStatus) {
    return given()
      .spec(getRequestSpecification())
      .when()
      .delete(resourcePath)
      .then()
      .log().ifValidationFails()
      .statusCode(expectedStatus)
      .extract();
  }

}
