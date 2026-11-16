
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType

// Pengaturan URL dan ID pembayaran
String baseUrl = 'http://localhost:8000'
String paymentId = '14'

// Locator login
TestObject emailField = new TestObject('emailField')
emailField.addProperty('xpath', ConditionType.EQUALS, "//input[@name='email']")

TestObject passwordField = new TestObject('passwordField')
passwordField.addProperty('xpath', ConditionType.EQUALS, "//input[@name='password']")

TestObject loginButton = new TestObject('loginButton')
loginButton.addProperty('xpath', ConditionType.EQUALS, "//button[@type='submit']")

// Locator form pembayaran
TestObject namaPengirim = new TestObject('namaPengirim')
namaPengirim.addProperty('id', ConditionType.EQUALS, 'nama_pengirim')

TestObject aplikasiField = new TestObject('aplikasiField')
aplikasiField.addProperty('id', ConditionType.EQUALS, 'aplikasi')

TestObject tombolKirim = new TestObject('tombolKirim')
tombolKirim.addProperty('id', ConditionType.EQUALS, 'btnKirim')

// Login
WebUI.openBrowser('')
WebUI.navigateToUrl(baseUrl + '/login')

WebUI.setText(emailField, 'bian@gmail.com')
WebUI.setText(passwordField, '12345678')
WebUI.click(loginButton)
WebUI.delay(3)

// Membuka halaman pembayaran
WebUI.navigateToUrl(baseUrl + '/pelanggan/pembayaran/' + paymentId)
WebUI.delay(3)

// Mengisi nama dan aplikasi pembayaran
WebUI.setText(namaPengirim, 'Aisyah')
WebUI.setText(aplikasiField, 'BCA')

// Sengaja tidak mengunggah file bukti pembayaran

// Klik tombol kirim
WebUI.click(tombolKirim)
WebUI.delay(3)
