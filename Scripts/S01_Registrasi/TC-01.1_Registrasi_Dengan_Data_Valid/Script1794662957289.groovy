import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration

def obj(String xp) {
	TestObject t = new TestObject(xp)
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S01_Registrasi/'

WebUI.openBrowser('')
WebUI.maximizeWindow()

// Langkah 1: Buka halaman Register Pelanggan
WebUI.navigateToUrl('http://127.0.0.1:8000/register')
WebUI.delay(2)
WebUI.takeScreenshot(shot + 'TC-01.1_Gambar1_halaman_register.png')

// Langkah 2: Isi seluruh field dengan data valid
WebUI.setText(obj("//input[@placeholder='Masukkan nama lengkap Anda']"), 'Rina Oktavia')
WebUI.setText(obj("//input[@type='email']"), 'rina.oktavia@gmail.com')
WebUI.setText(obj("//input[@placeholder='081234567890']"), '081234567890')
WebUI.setText(obj("//*[@placeholder='Masukkan alamat pengiriman']"), 'Jl. Melati No. 12, Nganjuk')
WebUI.setText(obj("(//input[@type='password'])[1]"), 'Bunga#2026')
WebUI.setText(obj("//input[@placeholder='Ulangi password']"), 'Bunga#2026')
WebUI.takeScreenshot(shot + 'TC-01.1_Gambar2_form_terisi.png')

// Langkah 3: Centang persetujuan syarat & ketentuan
WebUI.click(obj("//input[@type='checkbox']"))
WebUI.takeScreenshot(shot + 'TC-01.1_Gambar3_syarat_dicentang.png')

// Langkah 4: Klik tombol Daftar Sekarang
WebUI.click(obj("//button[contains(.,'Daftar Sekarang')]"))
WebUI.delay(3)
WebUI.verifyMatch(WebUI.getUrl(), '.*login.*', true)
WebUI.takeScreenshot(shot + 'TC-01.1_Gambar4_setelah_daftar.png')

WebUI.closeBrowser()