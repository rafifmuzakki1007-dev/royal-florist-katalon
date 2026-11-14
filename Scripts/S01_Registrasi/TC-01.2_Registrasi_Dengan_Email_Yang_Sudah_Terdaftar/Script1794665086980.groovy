import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

def obj = { String xp ->
	TestObject t = new TestObject(xp)
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S01_Registrasi/'
String url  = 'http://127.0.0.1:8000/register'

def valid = [nama:'Rina Oktavia', email:'rina.oktavia@gmail.com', hp:'081234567890',
			 alamat:'Jl. Melati No. 12, Nganjuk', pass:'Bunga#2026', konf:'Bunga#2026']

def isi = { Map d ->
	WebUI.setText(obj("//input[@placeholder='Masukkan nama lengkap Anda']"), d.nama)
	WebUI.setText(obj("//input[@type='email']"), d.email)
	WebUI.setText(obj("//input[@placeholder='081234567890']"), d.hp)
	WebUI.setText(obj("//*[@placeholder='Masukkan alamat pengiriman']"), d.alamat)
	WebUI.setText(obj("(//input[@type='password'])[1]"), d.pass)
	WebUI.setText(obj("//input[@placeholder='Ulangi password']"), d.konf)
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

// ===== A: sesuai Excel TC-01.2 =====
// Langkah 1: Buka halaman Register Pelanggan
WebUI.navigateToUrl(url)
WebUI.delay(2)
WebUI.takeScreenshot(shot + 'TC-01.2_Gambar1_halaman_register.png')

// Langkah 2: Isi form dengan email yang sudah terdaftar dan data lain valid
isi(valid)
WebUI.takeScreenshot(shot + 'TC-01.2_Gambar2_form_terisi.png')

// Langkah 3: Centang persetujuan syarat & ketentuan
WebUI.click(obj("//input[@type='checkbox']"))
WebUI.takeScreenshot(shot + 'TC-01.2_Gambar3_syarat_dicentang.png')

// Langkah 4: Klik tombol Daftar Sekarang
WebUI.click(obj("//button[contains(.,'Daftar Sekarang')]"))
WebUI.delay(2)
String akhirA = WebUI.getUrl()
WebUI.takeScreenshot(shot + 'TC-01.2_Gambar4_setelah_daftar.png')

// Expected: ditolak, tetap di halaman Register
if (!akhirA.contains('register')) {
	KeywordUtil.markWarning("[A] Email sudah terdaftar tetap DITERIMA. URL akhir: " + akhirA)
}
WebUI.verifyMatch(akhirA, '.*register.*', true, FailureHandling.CONTINUE_ON_FAILURE)

// ===== B: variasi email duplikat (pencari bug) =====
def variasi = [
	[id:'B1', desc:'Email duplikat huruf kapital semua',    email:'RINA.OKTAVIA@GMAIL.COM'],
	[id:'B2', desc:'Email duplikat huruf kapital sebagian', email:'Rina.Oktavia@gmail.com']
]
for (v in variasi) {
	WebUI.navigateToUrl(url)
	isi(valid + [email: v.email])
	WebUI.click(obj("//input[@type='checkbox']"))
	WebUI.click(obj("//button[contains(.,'Daftar Sekarang')]"))
	WebUI.delay(2)
	if (WebUI.waitForAlert(1, FailureHandling.OPTIONAL)) { WebUI.acceptAlert(FailureHandling.OPTIONAL) }
	String akhir = WebUI.getUrl()
	WebUI.takeScreenshot(shot + 'TC-01.2_' + v.id + '_sesudah.png')
	if (!akhir.contains('register')) {
		KeywordUtil.markWarning("[${v.id}] ${v.desc}: DITERIMA, seharusnya ditolak. URL akhir: " + akhir)
	}
	WebUI.verifyMatch(akhir, '.*register.*', true, FailureHandling.CONTINUE_ON_FAILURE)
}

WebUI.closeBrowser()