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

def valid = [nama:'Uji Server', email:'', hp:'081234567890',
			 alamat:'Jl. Melati No. 12, Nganjuk', pass:'Bunga#2026', konf:'Bunga#2026', setuju:true]

def uji = { String id, String desc, Map d ->
	WebUI.navigateToUrl(url)
	// matikan validasi browser supaya yang diuji adalah validasi server
	WebUI.executeJavaScript("document.querySelector('form').setAttribute('novalidate','novalidate')", null)
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

	String akhir = WebUI.getUrl()
	def pesan = WebUI.executeJavaScript("return Array.from(document.querySelectorAll('[class*=error],[class*=invalid],[class*=danger],[role=alert]')).map(e=>e.innerText.trim()).filter(t=>t).join(' | ')", null)
	def halaman = WebUI.executeJavaScript("return document.body.innerText.substring(0,400)", null)
	KeywordUtil.logInfo("[${id}] ${desc} | URL: ${akhir} | pesan: ${pesan}")
	if (halaman.contains('SQLSTATE') || halaman.contains('Server Error') || halaman.contains('Whoops') || halaman.contains('Exception')) {
		KeywordUtil.markWarning("[${id}] ${desc}: HALAMAN ERROR SERVER / DETAIL TEKNIS TAMPIL")
	}
	WebUI.takeScreenshot(shot + 'EKSPLORASI_SERVER_' + id + '.png')
	if (!akhir.contains('register')) {
		KeywordUtil.markWarning("[${id}] ${desc}: DITERIMA SERVER, seharusnya ditolak")
	}
	WebUI.verifyMatch(akhir, '.*register.*', true, FailureHandling.CONTINUE_ON_FAILURE)
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

uji('S1', 'Seluruh field kosong (tanpa validasi browser)', [nama:'', email:'', hp:'', alamat:'', pass:'', konf:'', setuju:false])
uji('S2', 'Email tanpa @',            valid + [email:'uji.s2.gmail.com'])
uji('S3', 'Konfirmasi password beda', valid + [email:'uji.s3@gmail.com', konf:'Bunga#2027'])
uji('S4', 'Password 1 karakter',      valid + [email:'uji.s4@gmail.com', pass:'a', konf:'a'])
uji('S5', 'Telepon berisi huruf',     valid + [email:'uji.s5@gmail.com', hp:'abcdefghij'])
uji('S6', 'Nama hanya spasi',         valid + [email:'uji.s6@gmail.com', nama:'   '])
uji('S7', 'Nomor telepon sama dengan akun Rina', valid + [email:'uji.s7@gmail.com', hp:'081234567890'])
uji('S8', 'Syarat tidak dicentang',   valid + [email:'uji.s8@gmail.com', setuju:false])
uji('S9', 'Input SQL pada nama',      valid + [email:'uji.s9@gmail.com', nama:"x'; DROP TABLE users;--"])

WebUI.closeBrowser()