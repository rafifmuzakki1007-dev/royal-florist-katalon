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

def valid = [nama:'Uji Negatif', email:'', hp:'081234567890',
			 alamat:'Jl. Melati No. 12, Nganjuk', pass:'Bunga#2026', konf:'Bunga#2026', setuju:true]

def uji = { String id, String desc, Map d ->
	WebUI.navigateToUrl(url)
	if (d.nama)   WebUI.setText(obj("//input[@placeholder='Masukkan nama lengkap Anda']"), d.nama)
	if (d.email)  WebUI.setText(obj("//input[@type='email']"), d.email)
	if (d.hp)     WebUI.setText(obj("//input[@placeholder='081234567890']"), d.hp)
	if (d.alamat) WebUI.setText(obj("//*[@placeholder='Masukkan alamat pengiriman']"), d.alamat)
	if (d.pass)   WebUI.setText(obj("(//input[@type='password'])[1]"), d.pass)
	if (d.konf)   WebUI.setText(obj("//input[@placeholder='Ulangi password']"), d.konf)
	if (d.setuju) WebUI.click(obj("//input[@type='checkbox']"))

	WebUI.click(obj("//button[contains(.,'Daftar Sekarang')]"))
	WebUI.delay(2)
	if (WebUI.waitForAlert(1, FailureHandling.OPTIONAL)) { WebUI.acceptAlert(FailureHandling.OPTIONAL) }

	def pesan = WebUI.executeJavaScript("var e=document.querySelector(':invalid'); return e ? ((e.name||e.type)+': '+e.validationMessage) : 'tidak ada validasi bawaan browser'", null)
	KeywordUtil.logInfo("[${id}] ${desc} | validasi browser: ${pesan}")

	String akhir = WebUI.getUrl()
	WebUI.takeScreenshot(shot + 'TC-01.3_' + id + '_sesudah.png')
	if (!akhir.contains('register')) {
		KeywordUtil.markWarning("[${id}] ${desc}: DITERIMA, seharusnya ditolak. URL akhir: " + akhir)
	}
	WebUI.verifyMatch(akhir, '.*register.*', true, FailureHandling.CONTINUE_ON_FAILURE)
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

// ===== A: sesuai Excel TC-01.3 =====
// Langkah 1: Buka halaman Register Pelanggan
WebUI.navigateToUrl(url)
WebUI.delay(2)
WebUI.takeScreenshot(shot + 'TC-01.3_Gambar1_halaman_register.png')

// Langkah 2: Biarkan seluruh field kosong
WebUI.takeScreenshot(shot + 'TC-01.3_Gambar2_field_kosong.png')

// Langkah 3: Klik tombol Daftar Sekarang
WebUI.click(obj("//button[contains(.,'Daftar Sekarang')]"))
WebUI.delay(2)
def pesanA = WebUI.executeJavaScript("var e=document.querySelector(':invalid'); return e ? ((e.name||e.type)+': '+e.validationMessage) : 'tidak ada validasi bawaan browser'", null)
KeywordUtil.logInfo("[A] Seluruh field kosong | validasi browser: ${pesanA}")
String akhirA = WebUI.getUrl()
WebUI.takeScreenshot(shot + 'TC-01.3_Gambar3_setelah_daftar.png')
if (!akhirA.contains('register')) {
	KeywordUtil.markWarning("[A] Seluruh field kosong DITERIMA. URL akhir: " + akhirA)
}
WebUI.verifyMatch(akhirA, '.*register.*', true, FailureHandling.CONTINUE_ON_FAILURE)

// ===== B: satu field dikosongkan, field lain valid =====
uji('B1', 'Nama kosong',            valid + [nama:'',   email:'uji.k3.b1@gmail.com'])
uji('B2', 'Email kosong',           valid)
uji('B3', 'Telepon kosong',         valid + [hp:'',     email:'uji.k3.b3@gmail.com'])
uji('B4', 'Alamat kosong',          valid + [alamat:'', email:'uji.k3.b4@gmail.com'])
uji('B5', 'Password kosong',        valid + [pass:'',   konf:'', email:'uji.k3.b5@gmail.com'])
uji('B6', 'Konfirmasi kosong',      valid + [konf:'',   email:'uji.k3.b6@gmail.com'])
uji('B7', 'Syarat tidak dicentang', valid + [setuju:false, email:'uji.k3.b7@gmail.com'])

// ===== C: variasi data tidak valid (tambahan pencari bug) =====
uji('C1', 'Email tanpa @',            valid + [email:'uji.k3.c1.gmail.com'])
uji('C2', 'Konfirmasi password beda', valid + [konf:'Bunga#2027', email:'uji.k3.c2@gmail.com'])
uji('C3', 'Password 1 karakter',      valid + [pass:'a', konf:'a', email:'uji.k3.c3@gmail.com'])
uji('C4', 'Telepon berisi huruf',     valid + [hp:'abcdefghij', email:'uji.k3.c4@gmail.com'])
uji('C5', 'Telepon terlalu pendek',   valid + [hp:'08', email:'uji.k3.c5@gmail.com'])
uji('C6', 'Nama hanya spasi',         valid + [nama:'   ', email:'uji.k3.c6@gmail.com'])
uji('C7', 'Nama berisi tag HTML',     valid + [nama:'<b>Uji</b>', email:'uji.k3.c7@gmail.com'])

WebUI.closeBrowser()