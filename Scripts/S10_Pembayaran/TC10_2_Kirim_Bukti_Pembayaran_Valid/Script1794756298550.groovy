
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType

// Pengaturan URL dan ID pembayaran
String baseUrl = 'http://localhost:8000'
String paymentId = '13'

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

TestObject buktiField = new TestObject('buktiField')
buktiField.addProperty('id', ConditionType.EQUALS, 'bukti')

TestObject tombolKirim = new TestObject('tombolKirim')
tombolKirim.addProperty('id', ConditionType.EQUALS, 'btnKirim')

// Membuka browser dan login
WebUI.openBrowser('')
WebUI.navigateToUrl(baseUrl + '/login')

WebUI.setText(emailField, 'bian@gmail.com')
WebUI.setText(passwordField, '12345678')
WebUI.click(loginButton)
WebUI.delay(3)

// Membuka halaman pembayaran
WebUI.navigateToUrl(baseUrl + '/pelanggan/pembayaran/' + paymentId)
WebUI.delay(3)

// Mengisi nama pengirim
WebUI.setText(namaPengirim, 'Aisyah')

// Mengisi aplikasi pembayaran
WebUI.setText(aplikasiField, 'BCA')

// Upload bukti pembayaran
WebUI.uploadFile(buktiField, 'C:\\Users\\LENOVO LOQ\\Downloads\\bukti_bayar.jpg')

// Klik tombol kirim
WebUI.click(tombolKirim)
WebUI.delay(3)

// Verifikasi halaman riwayat pesanan
WebUI.verifyMatch(
    WebUI.getUrl(),
    '.*riwayat.*',
    true
)

WebUI.closeBrowser()
