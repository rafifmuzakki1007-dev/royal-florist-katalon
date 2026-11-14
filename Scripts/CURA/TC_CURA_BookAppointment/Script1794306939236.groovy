import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

WebUI.openBrowser('')
WebUI.navigateToUrl('https://katalon-demo-cura.herokuapp.com/profile.php#login')
WebUI.setText(findTestObject('Object Repository/CURA/txt_Username'), 'John Doe')
WebUI.setText(findTestObject('Object Repository/CURA/txt_Password'), 'ThisIsNotAPassword')
WebUI.click(findTestObject('Object Repository/CURA/btn_Login'))
WebUI.verifyElementPresent(findTestObject('Object Repository/CURA/select_Facility'), 10)
WebUI.selectOptionByLabel(findTestObject('Object Repository/CURA/select_Facility'), 'Tokyo CURA Healthcare Center', false)
WebUI.setText(findTestObject('Object Repository/CURA/txt_VisitDate'), '31/12/2030')
WebUI.setText(findTestObject('Object Repository/CURA/txt_Comment'), 'Happy path appointment')
WebUI.click(findTestObject('Object Repository/CURA/btn_BookAppointment'))
WebUI.verifyElementPresent(findTestObject('Object Repository/CURA/lbl_AppointmentConfirmation'), 10)
WebUI.closeBrowser()