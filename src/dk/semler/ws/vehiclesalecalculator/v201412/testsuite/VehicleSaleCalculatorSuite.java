package dk.semler.ws.vehiclesalecalculator.v201412.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dk.semler.ws.vehiclesalecalculator.v201412.testcases.CalculateNewVehicleTestCases;
import dk.semler.ws.vehiclesalecalculator.v201412.testcases.CalculateUsedVehicleTestCases;

//=====================================================
//=       test suite to run test classes			  =
//=====================================================

@RunWith(Suite.class)
@SuiteClasses({ 
	CalculateNewVehicleTestCases.class,
	CalculateUsedVehicleTestCases.class 
})
public class VehicleSaleCalculatorSuite {

}
