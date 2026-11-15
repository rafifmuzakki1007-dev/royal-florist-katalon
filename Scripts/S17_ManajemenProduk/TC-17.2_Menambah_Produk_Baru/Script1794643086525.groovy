import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.llm.keyword.LlmKeywords as LLM
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

WebUI.openBrowser('')

WebUI.navigateToUrl('http://localhost:8000/login')

WebUI.setText(findTestObject('S17_ManajemenProduk/Page_Login - Royal Florist/input_Email'), 'adminflorist@gmail.com')

WebUI.sendKeys(findTestObject('S17_ManajemenProduk/Page_Login - Royal Florist/input_Password'), Keys.chord(Keys.ENTER))

WebUI.waitForElementPresent(findTestObject('S17_ManajemenProduk/Page_Dashboard Admin - Royal Florist/a_Produk'), 10)

WebUI.click(findTestObject('S17_ManajemenProduk/Page_Dashboard Admin - Royal Florist/a_Produk'))

WebUI.click(findTestObject('Page_Admin - Royal Florist/a_Produk Baru'))

WebUI.selectOptionByLabel(findTestObject('Page_Admin - Royal Florist/select_Pilih kategori                                    Buket                                    Hantaran                                    Papan Bunga'), 
    'Buket', true)

WebUI.selectOptionByLabel(findTestObject('Page_Admin - Royal Florist/select_Pilih jenisBuket SatinBuket UangBuke_e04651'), 
    'Buket Fresh Flower', true)

WebUI.setText(findTestObject('Page_Admin - Royal Florist/input_Nama bunga (mis. Aster, Mawar)_nama_produk'), 'Lily Putih')

WebUI.setText(findTestObject('Page_Admin - Royal Florist/input_Harga jual (Rp)_harga'), '120000')

WebUI.setText(findTestObject('Page_Admin - Royal Florist/input_Stok (tangkai)_stok'), '10')

WebUI.uploadFile(findTestObject('Page_Admin - Royal Florist/input_Foto produk_foto_produk'), 'D:/royal-florist-katalon/Test Data/images/buket-lily-putih.jpg')

WebUI.click(findTestObject('Page_Admin - Royal Florist/button_Simpan produk'))

WebUI.waitForElementPresent(findTestObject('Page_Admin - Royal Florist/span_Lily Putih'), 10)

WebUI.verifyElementText(findTestObject('Page_Admin - Royal Florist/span_Lily Putih'), 'Lily Putih')

