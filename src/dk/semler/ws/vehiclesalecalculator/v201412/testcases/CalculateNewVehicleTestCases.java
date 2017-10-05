package dk.semler.ws.vehiclesalecalculator.v201412.testcases;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.CALCULATIONTYPEType;
import dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.ECONOMYType;
import dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.VEHICLEType;
import dk.semler.ws.vehiclesalecalculator.v201412.stubs.ExecuteServiceRequest;
import dk.semler.ws.vehiclesalecalculator.v201412.stubs.XMLService;

@RunWith(Parameterized.class)
public class CalculateNewVehicleTestCases {
	
	private static XMLService vehicleSaleCalculatorService;	
	private static JAXBContext jaxbContextInput;	
	private static JAXBContext jaxbContextOutput;
	
	private String inputmodelcode;
	private String inputmodelyear;
	private String inputfactoryequipment;
	private String inputpricecode;
	private String inputcalculationtype;
	private String inputtaxcode;
	private String expectedsalespriceincltaxexclvat;
	private String expectedsalespriceincltaxinclvat;
	private String expecteddealermargingexclequipmentincltaxexclvat;
	private String expectedtaxexclregulation;
	private String expectedgrandtotalpriceexclvat;
	private String expectedgrandtotalpricevat;
	private String expectedgrandtotalpriceinclvat;
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	
	private dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.MESSAGE requestMessage;	
	private dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.response.MESSAGE responseMessage;
	
	//=====================================================
	//=    constructor reflecting data from csv file	  =
	//=====================================================
	
	public CalculateNewVehicleTestCases(String inputmodelcode, String inputmodelyear, String inputfactoryequipment, 
			String inputpricecode,                                  
			String inputcalculationtype,                            
			String inputtaxcode,                            
			String expectedsalespriceincltaxexclvat,               
			String expectedsalespriceincltaxinclvat,               
			String expecteddealermargingexclequipmentincltaxexclvat,
			String expectedtaxexclregulation	,
			String expectedgrandtotalpriceexclvat,
	        String expectedgrandtotalpricevat,
	        String expectedgrandtotalpriceinclvat			
			){
		this.inputmodelcode = inputmodelcode;
		this.inputmodelyear = inputmodelyear;
		this.inputfactoryequipment = inputfactoryequipment;
		this.inputpricecode = inputpricecode;                                  
		this.inputcalculationtype = inputcalculationtype;                            
		this.inputtaxcode = inputtaxcode;                                  
		this.expectedsalespriceincltaxexclvat = expectedsalespriceincltaxexclvat;               
		this.expectedsalespriceincltaxinclvat = expectedsalespriceincltaxinclvat;               
		this.expecteddealermargingexclequipmentincltaxexclvat = expecteddealermargingexclequipmentincltaxexclvat;
		this.expectedtaxexclregulation = expectedtaxexclregulation;   
		this.expectedgrandtotalpriceexclvat = expectedgrandtotalpriceexclvat;
		this.expectedgrandtotalpricevat = expectedgrandtotalpricevat;
		this.expectedgrandtotalpriceinclvat = expectedgrandtotalpriceinclvat;                       
	}
	
	//=====================================================
	//=    scan csv file for input and expected data      =
	//=====================================================
	
	@Parameters
	public static List<Object[]> data(){
		List<Object[]> content = new ArrayList<>();
		File file = new File(CalculateNewVehicleTestCases.class.getResource("/calculatenewvehicleinput.csv").getFile());
		try(Scanner scanner = new Scanner(file)){
			scanner.nextLine();
	        while (scanner.hasNextLine()) {
	            content.add(scanner.nextLine().split(";"));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
	
	//=====================================================
	//=    resolve dependencies for service to be test    =
	//=====================================================

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QName qname = new QName("http://www.semlernet.dk/xmlns/xmlservice/200903/", "XMLService200903");
		Service service = Service.create(CalculateNewVehicleTestCases.class.getResource("/client/VehicleSaleCalculator.v201412.wsdl"), qname);
		vehicleSaleCalculatorService = service.getPort(XMLService.class);
		BindingProvider bp = (BindingProvider) vehicleSaleCalculatorService;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://testesb.semlernet.dk/xs/201412/VehicleSaleCalculator");
		jaxbContextInput = JAXBContext.newInstance(dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.MESSAGE.class);
		jaxbContextOutput = JAXBContext.newInstance(dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.response.MESSAGE.class);
	}
	
	//===============================================================================
	//=    make a call to service and get response to be available for each test    =
	//===============================================================================
	
	@Before
	public void setUp() throws Exception {
		dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.MESSAGE.REQUEST request = 
				new dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.MESSAGE.REQUEST();

		request.withDTD(request.getDTD());
		request.withNAME(request.getNAME());
		request.withDEALER("00001");
		request.withUSERID("Priceadapter");

		VEHICLEType vehicleType = new VEHICLEType();
		vehicleType.setMODELCODE(inputmodelcode);
		vehicleType.setMODELYEAR(Integer.parseInt(inputmodelyear));			
		vehicleType.setPRICECODE(inputpricecode);

		String[] factoryEquipments = inputfactoryequipment.split(Pattern.quote("|"));
		if(factoryEquipments.length > 0){
			for(String equipment : factoryEquipments){
				vehicleType.withFACTORYEQUIPMENT(equipment);					
			}
		}			
		request.withVEHICLE(vehicleType);

		ECONOMYType economyType = new ECONOMYType();
		economyType.setDELIVERYEXPENSES(new BigDecimal(0.00));
		economyType.setLICENSETAGFEE(new BigDecimal(0.00));
		request.withECONOMY(economyType);

		CALCULATIONTYPEType calculationType = new CALCULATIONTYPEType();
		calculationType.setCALCULATIONTYPE(inputcalculationtype);
		calculationType.setEXCLUDEAUTOMATICSEARCHOFSTANDARDEQUIPMENT(true);
		calculationType.setEXCLUDEAUTOMATICSEARCHOFPLUSPACKAGES(true);
		calculationType.setTAXCODE(inputtaxcode);
		request.withCALCULATIONTYPE(calculationType);

		requestMessage = new dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.request.MESSAGE();
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
		responseMessage = (dk.semler.ws.vehiclesalecalculator.v201412.calculatenewvehicle.response.MESSAGE) 
				unmarshal.unmarshal(new StringReader(vehicleSaleCalculatorService.executeService(vehiclePriceRequest).getOutputMessage()));
		if(responseMessage.getRESPONSE().getCALCULATION() == null)
			Assert.fail(responseMessage.getRESPONSE().getERROR().getDESCRIPTION());
	}
	
	//=====================================================
	//=    				series of tests    				  =
	//=====================================================

	@Test
	public void PassatVanHybridInclTax() {
		collector.checkThat("expectedsalespriceincltaxexclvat",
				responseMessage.getRESPONSE().getCALCULATION().getSALESPRICE().getINCLTAXEXCLVAT().setScale(2),
				IsEqual.equalTo(new BigDecimal(expectedsalespriceincltaxexclvat).setScale(2)));
		collector.checkThat("expectedtaxexclregulation",
				responseMessage.getRESPONSE().getCALCULATION().getTAXEXCLREGULATIONS().setScale(2),
				IsEqual.equalTo(new BigDecimal(expectedtaxexclregulation).setScale(2)));
		collector.checkThat("expectedsalespriceincltaxinclvat",
				responseMessage.getRESPONSE().getCALCULATION().getSALESPRICE().getINCLTAXINCLVAT().setScale(2),
				IsEqual.equalTo(new BigDecimal(expectedsalespriceincltaxinclvat).setScale(2)));
		collector.checkThat("expecteddealermargingexclequipmentincltaxexclvat",
				responseMessage.getRESPONSE().getCALCULATION().getDEALERMARGINEXCLEQUIPMENT().getEXCLTAXEXCLVAT().setScale(2),
				IsEqual.equalTo(new BigDecimal(expecteddealermargingexclequipmentincltaxexclvat).setScale(2)));
		collector.checkThat("expectedgrandtotalpricevat",
				responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getVAT().setScale(2),
				IsEqual.equalTo(new BigDecimal(expectedgrandtotalpricevat).setScale(2)));
		collector.checkThat("expectedgrandtotalpriceinclvat",
				responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getINCLTAXINCLVAT().setScale(2),
				IsEqual.equalTo(new BigDecimal(expectedgrandtotalpriceinclvat).setScale(2)));
		collector.checkThat("expectedgrandtotalpriceexclvat",
				responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getINCLTAXEXCLVAT().setScale(2),
				IsEqual.equalTo(new BigDecimal(expectedgrandtotalpriceexclvat).setScale(2)));
		
		/*assertEquals("expectedtaxexclregulation",new BigDecimal(expectedtaxexclregulation).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getTAXEXCLREGULATIONS().setScale(2));
		assertEquals("",new BigDecimal(expectedsalespriceincltaxexclvat).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getSALESPRICE().getINCLTAXEXCLVAT().setScale(2));
		assertEquals(new BigDecimal(expectedsalespriceincltaxinclvat).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getSALESPRICE().getINCLTAXINCLVAT().setScale(2));
		assertEquals(new BigDecimal(expecteddealermargingexclequipmentincltaxexclvat).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getDEALERMARGINEXCLEQUIPMENT().getEXCLTAXEXCLVAT().setScale(2));
		assertEquals(new BigDecimal(expectedgrandtotalpriceexclvat).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getINCLTAXEXCLVAT().setScale(2));
		assertEquals(new BigDecimal(expectedgrandtotalpricevat).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getVAT().setScale(2));
		assertEquals("expectedgrandtotalpriceinclvat",new BigDecimal(expectedgrandtotalpriceinclvat).setScale(2), 
				responseMessage.getRESPONSE().getCALCULATION().getGRANDTOTALPRICE().getINCLTAXINCLVAT().setScale(2));*/
	}
	
	//=====================================================
	//=    reset response after each test case		      =
	//=====================================================
	
	@After
	public void tearDown(){
		responseMessage = null;
	}

}
