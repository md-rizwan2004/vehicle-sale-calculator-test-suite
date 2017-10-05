package dk.semler.ws.vehiclesalecalculator.v201412.testcases;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.request.ECONOMYType;
import dk.semler.ws.vehiclesalecalculator.v201412.stubs.ExecuteServiceRequest;
import dk.semler.ws.vehiclesalecalculator.v201412.stubs.XMLService;

@RunWith(Parameterized.class)
public class CalculateUsedVehicleTestCases {
	
	private static XMLService vehicleSaleCalculatorService;	
	private static JAXBContext jaxbContextInput;	
	private static JAXBContext jaxbContextOutput;
	
	private String inputvehicleid;
	private String inputvehiclesaleprice;
	private String inputdeliveryexpenses;
	private String inputlicensetagfee;
	private String expectedtotalpriceinclvat;
	
	private dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.request.MESSAGE requestMessage;	
	private dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.response.MESSAGE responseMessage;
	
	public CalculateUsedVehicleTestCases(String inputvehicleid, String inputvehiclesaleprice, String inputdeliveryexpenses, 
			String inputlicensetagfee, String expectedtotalpriceinclvat){
		this.inputvehicleid = inputvehicleid;
		this.inputvehiclesaleprice = inputvehiclesaleprice;
		this.inputdeliveryexpenses = inputdeliveryexpenses;
		this.inputlicensetagfee = inputlicensetagfee;
		this.expectedtotalpriceinclvat = expectedtotalpriceinclvat;
	}
	
	@Parameters
	public static List<Object[]> data(){
		List<Object[]> content = new ArrayList<>();
		File file = new File(CalculateUsedVehicleTestCases.class.getResource("/calculateusedvehicleinput.csv").getFile());
		try(Scanner scanner = new Scanner(file)){
			scanner.nextLine();
	        while (scanner.hasNextLine()) {
	            content.add(scanner.nextLine().split(","));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QName qname = new QName("http://www.semlernet.dk/xmlns/xmlservice/200903/", "XMLService200903");
		Service service = Service.create(CalculateUsedVehicleTestCases.class.getResource("/client/VehicleSaleCalculator.v201412.wsdl"), qname);
		vehicleSaleCalculatorService = service.getPort(XMLService.class);
		BindingProvider bp = (BindingProvider) vehicleSaleCalculatorService;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://testesb.semlernet.dk/xs/201412/VehicleSaleCalculator");
		jaxbContextInput = JAXBContext.newInstance(dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.request.MESSAGE.class);
		jaxbContextOutput = JAXBContext.newInstance(dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.response.MESSAGE.class);
	}

	@Before
	public void setUp() throws Exception {
		dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.request.MESSAGE.REQUEST request = 
				new dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.request.MESSAGE.REQUEST();

		request.withDTD(request.getDTD());
		request.withNAME(request.getNAME());
		request.withDEALER("00069");
		request.withUSERID("extthtb@semler.dk");
		request.withVEHICLEID(inputvehicleid);

		ECONOMYType economyType = new ECONOMYType();
		economyType.setVEHICLESALEPRICE(new BigDecimal(inputvehiclesaleprice));
		economyType.setDELIVERYEXPENSES(new BigDecimal(inputdeliveryexpenses));
		economyType.setLICENSETAGFEE(new BigDecimal(inputlicensetagfee));
		request.withECONOMY(economyType);

		request.withCALCULATIONTYPE("CAR");

		requestMessage = new dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.request.MESSAGE();
		requestMessage.withREQUEST(request);				

		StringWriter requestMessageXML = new StringWriter();			
		try {
			Marshaller marshaller = jaxbContextInput.createMarshaller();
			marshaller.marshal(requestMessage, requestMessageXML);
		} catch (JAXBException e) {
			throw e;
		}

		ExecuteServiceRequest vehiclePriceRequest = new ExecuteServiceRequest();
		vehiclePriceRequest.setConsumerId("TESTCONSUMER ");			
		vehiclePriceRequest.setInputMessage(requestMessageXML.toString());

		Unmarshaller unmarshal = jaxbContextOutput.createUnmarshaller();
		responseMessage = (dk.semler.ws.vehiclesalecalculator.v201412.calculateusedvehicle.response.MESSAGE) 
				unmarshal.unmarshal(new StringReader(vehicleSaleCalculatorService.executeService(vehiclePriceRequest).getOutputMessage()));
		if(responseMessage.getRESPONSE().getCALCULATION() == null)
			Assert.fail(responseMessage.getRESPONSE().getERROR().getDESCRIPTION());
	}

	@Test
	public void VehicleGrandTotalPriceInclVATShouldMatch() {
		assertEquals(new BigDecimal(expectedtotalpriceinclvat).setScale(2), 
				(responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getINCLUDINGVAT().setScale(2)));
	}

	@After
	public void tearDown() throws Exception {
		responseMessage = null;
	}

}
