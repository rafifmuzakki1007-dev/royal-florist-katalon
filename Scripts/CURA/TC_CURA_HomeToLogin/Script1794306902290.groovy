import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

WebUI.openBrowser('')
WebUI.navigateToUrl('https://katalon-demo-cura.herokuapp.com/')
WebUI.verifyElementPresent(findTestObject('Object Repository/CURA/btn_MakeAppointment'), 10)
WebUI.click(findTestObject('Object Repository/CURA/btn_MakeAppointment'))
WebUI.verifyElementPresent(findTestObject('Object Repository/CURA/txt_Username'), 10)
WebUI.closeBrowser()