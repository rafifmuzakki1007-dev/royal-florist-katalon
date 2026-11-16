
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.model.FailureHandling


// Pengaturan URL dan ID pembayaran
String baseUrl = 'http://localhost:8000'
String paymentId = '14'

// Membuat locator untuk elemen login
TestObject emailField = new TestObject('emailField')
emailField.addProperty('xpath', ConditionType.EQUALS, "//input[@name='email']")

TestObject passwordField = new TestObject('passwordField')
passwordField.addProperty('xpath', ConditionType.EQUALS, "//input[@name='password']")

TestObject loginButton = new TestObject('loginButton')
loginButton.addProperty('xpath', ConditionType.EQUALS, "//button[@type='submit']")

TestObject totalTagihan = new TestObject('totalTagihan')
totalTagihan.addProperty('xpath', ConditionType.EQUALS, "//div[@class='total']/b")

TestObject countdownField = new TestObject('countdownField')
countdownField.addProperty('id', ConditionType.EQUALS, 'hitung')


// Membuka browser dan halaman login
WebUI.openBrowser('')
WebUI.navigateToUrl(baseUrl + '/login')

// Mengisi form login
WebUI.setText(emailField, 'bian@gmail.com')
WebUI.setText(passwordField, '12345678')

// Klik tombol Masuk
WebUI.click(loginButton)
WebUI.delay(3)

// Membuka halaman pembayaran setelah login

WebUI.navigateToUrl(baseUrl + '/pelanggan/pembayaran/' + paymentId)
WebUI.delay(3)

// Memeriksa format countdown pembayaran
String countdown = WebUI.getText(countdownField)

WebUI.verifyMatch(
    countdown,
    '^\\d{2}:\\d{2}:\\d{2}$',
    true,
	FailureHandling.CONTINUE_ON_FAILURE
)

// Memeriksa nominal total tagihan
WebUI.verifyElementText(totalTagihan, 'Rp 1.020.000')

// Memeriksa informasi pembayaran
WebUI.verifyTextPresent('Pembayaran Pesanan', false)
WebUI.verifyTextPresent('Batas Waktu Pembayaran', false)
WebUI.verifyTextPresent('Total Tagihan', false)

// Menutup browser
WebUI.closeBrowser()
