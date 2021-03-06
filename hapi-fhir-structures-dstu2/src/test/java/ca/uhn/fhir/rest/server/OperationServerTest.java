package ca.uhn.fhir.rest.server;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.util.PortUtil;

/**
 * Created by dsotnikov on 2/25/2014.
 */
public class OperationServerTest {
	private static CloseableHttpClient ourClient;
	private static FhirContext ourCtx;

	private static StringDt ourLastParam1;
	private static Patient ourLastParam2;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(OperationServerTest.class);
	private static int ourPort;
	private static IdDt ourLastId;
	private static Server ourServer;
	private static String ourLastMethod;
	private static List<StringDt> ourLastParam3;

	@Before
	public void before() {
		ourLastParam1 = null;
		ourLastParam2 = null;
		ourLastParam3 = null;
		ourLastId = null;
		ourLastMethod = "";
	}

	@Test
	public void testOperationOnType() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("PARAM1").setValue(new StringDt("PARAM1val"));
		p.addParameter().setName("PARAM2").setResource(new Patient().setActive(true));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/Patient/$OP_TYPE");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertEquals(200, status.getStatusLine().getStatusCode());
		String response = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals("PARAM1val", ourLastParam1.getValue());
		assertEquals(true, ourLastParam2.getActive().booleanValue());
		assertEquals("$OP_TYPE", ourLastMethod);

		Parameters resp = ourCtx.newXmlParser().parseResource(Parameters.class, response);
		assertEquals("RET1", resp.getParameter().get(0).getName());
	}

	@Test
	public void testOperationOnTypeReturnBundle() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("PARAM1").setValue(new StringDt("PARAM1val"));
		p.addParameter().setName("PARAM2").setResource(new Patient().setActive(true));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/Patient/$OP_TYPE_RET_BUNDLE");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertEquals(200, status.getStatusLine().getStatusCode());
		String response = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals("PARAM1val", ourLastParam1.getValue());
		assertEquals(true, ourLastParam2.getActive().booleanValue());
		assertEquals("$OP_TYPE_RET_BUNDLE", ourLastMethod);

		Bundle resp = ourCtx.newXmlParser().parseResource(Bundle.class, response);
		assertEquals("100", resp.getEntryFirstRep().getTransactionResponse().getStatus());
	}


	@Test
	public void testOperationOnServer() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("PARAM1").setValue(new StringDt("PARAM1val"));
		p.addParameter().setName("PARAM2").setResource(new Patient().setActive(true));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/$OP_SERVER");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertEquals(200, status.getStatusLine().getStatusCode());
		String response = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals("PARAM1val", ourLastParam1.getValue());
		assertEquals(true, ourLastParam2.getActive().booleanValue());
		assertEquals("$OP_SERVER", ourLastMethod);

		Parameters resp = ourCtx.newXmlParser().parseResource(Parameters.class, response);
		assertEquals("RET1", resp.getParameter().get(0).getName());
	}

	@Test
	public void testOperationWithListParam() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("PARAM2").setResource(new Patient().setActive(true));
		p.addParameter().setName("PARAM3").setValue(new StringDt("PARAM3val1"));
		p.addParameter().setName("PARAM3").setValue(new StringDt("PARAM3val2"));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/$OP_SERVER_LIST_PARAM");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertEquals(200, status.getStatusLine().getStatusCode());
		String response = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals("$OP_SERVER_LIST_PARAM", ourLastMethod);
		assertEquals(true, ourLastParam2.getActive().booleanValue());
		assertEquals(null, ourLastParam1);
		assertEquals(2, ourLastParam3.size());
		assertEquals("PARAM3val1", ourLastParam3.get(0).getValue());
		assertEquals("PARAM3val2", ourLastParam3.get(1).getValue());

		Parameters resp = ourCtx.newXmlParser().parseResource(Parameters.class, response);
		assertEquals("RET1", resp.getParameter().get(0).getName());
	}

	@Test
	public void testOperationOnInstance() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("PARAM1").setValue(new StringDt("PARAM1val"));
		p.addParameter().setName("PARAM2").setResource(new Patient().setActive(true));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/Patient/123/$OP_INSTANCE");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertEquals(200, status.getStatusLine().getStatusCode());
		String response = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals("PARAM1val", ourLastParam1.getValue());
		assertEquals(true, ourLastParam2.getActive().booleanValue());
		assertEquals("123", ourLastId.getIdPart());
		assertEquals("$OP_INSTANCE", ourLastMethod);

		Parameters resp = ourCtx.newXmlParser().parseResource(Parameters.class, response);
		assertEquals("RET1", resp.getParameter().get(0).getName());
	}

	@Test
	public void testOperationWrongParamType() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("PARAM1").setValue(new IntegerDt("123"));
		p.addParameter().setName("PARAM2").setResource(new Patient().setActive(true));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost("http://localhost:" + ourPort + "/Patient/$OP_TYPE");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertEquals(400, status.getStatusLine().getStatusCode());
		String response = IOUtils.toString(status.getEntity().getContent());
		IOUtils.closeQuietly(status.getEntity().getContent());

		ourLog.info(status.getStatusLine().toString());
		ourLog.info(response);

		assertThat(response, containsString("Request has parameter PARAM1 of type IntegerDt but method expects type StringDt"));
	}

	@AfterClass
	public static void afterClass() throws Exception {
		ourServer.stop();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		ourCtx = FhirContext.forDstu2();
		ourPort = PortUtil.findFreePort();
		ourServer = new Server(ourPort);

		ServletHandler proxyHandler = new ServletHandler();
		RestfulServer servlet = new RestfulServer();
		servlet.setFhirContext(ourCtx);
		servlet.setResourceProviders(new PatientProvider());
		servlet.setPlainProviders(new PlainProvider());
		ServletHolder servletHolder = new ServletHolder(servlet);
		proxyHandler.addServletWithMapping(servletHolder, "/*");
		ourServer.setHandler(proxyHandler);
		ourServer.start();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connectionManager);
		ourClient = builder.build();

	}

	public static class PlainProvider {

		//@formatter:off
		@Operation(name="$OP_SERVER")
		public Parameters opServer(
				@OperationParam(name="PARAM1") StringDt theParam1,
				@OperationParam(name="PARAM2") Patient theParam2
				) {
			//@formatter:on

			ourLastMethod = "$OP_SERVER";
			ourLastParam1 = theParam1;
			ourLastParam2 = theParam2;

			Parameters retVal = new Parameters();
			retVal.addParameter().setName("RET1").setValue(new StringDt("RETVAL1"));
			return retVal;
		}

		//@formatter:off
		@Operation(name="$OP_SERVER_LIST_PARAM")
		public Parameters opServerListParam(
				@OperationParam(name="PARAM2") Patient theParam2,
				@OperationParam(name="PARAM3") List<StringDt> theParam3
				) {
			//@formatter:on

			ourLastMethod = "$OP_SERVER_LIST_PARAM";
			ourLastParam2 = theParam2;
			ourLastParam3 = theParam3;

			Parameters retVal = new Parameters();
			retVal.addParameter().setName("RET1").setValue(new StringDt("RETVAL1"));
			return retVal;
		}

	}

	public static class PatientProvider implements IResourceProvider {

		@Override
		public Class<? extends IResource> getResourceType() {
			return Patient.class;
		}

		//@formatter:off
		@Operation(name="$OP_TYPE")
		public Parameters opType(
				@OperationParam(name="PARAM1") StringDt theParam1,
				@OperationParam(name="PARAM2") Patient theParam2
				) {
			//@formatter:on

			ourLastMethod = "$OP_TYPE";
			ourLastParam1 = theParam1;
			ourLastParam2 = theParam2;

			Parameters retVal = new Parameters();
			retVal.addParameter().setName("RET1").setValue(new StringDt("RETVAL1"));
			return retVal;
		}

		//@formatter:off
		@Operation(name="$OP_TYPE_RET_BUNDLE")
		public Bundle opTypeRetBundle(
				@OperationParam(name="PARAM1") StringDt theParam1,
				@OperationParam(name="PARAM2") Patient theParam2
				) {
			//@formatter:on

			ourLastMethod = "$OP_TYPE_RET_BUNDLE";
			ourLastParam1 = theParam1;
			ourLastParam2 = theParam2;

			Bundle retVal = new Bundle();
			retVal.addEntry().getTransactionResponse().setStatus("100");
			return retVal;
		}

		//@formatter:off
		@Operation(name="$OP_INSTANCE")
		public Parameters opInstance(
				@IdParam IdDt theId,
				@OperationParam(name="PARAM1") StringDt theParam1,
				@OperationParam(name="PARAM2") Patient theParam2
				) {
			//@formatter:on

			ourLastMethod = "$OP_INSTANCE";
			ourLastId = theId;
			ourLastParam1 = theParam1;
			ourLastParam2 = theParam2;

			Parameters retVal = new Parameters();
			retVal.addParameter().setName("RET1").setValue(new StringDt("RETVAL1"));
			return retVal;
		}

	}

}
