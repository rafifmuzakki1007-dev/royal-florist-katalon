import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil
import org.openqa.selenium.Keys

// TC-15.3 : Mencari pelanggan yang tidak ditemukan (Negative)
// Data    : Kata kunci "zzzzz"
// Expected: pesan data tidak ditemukan, tanpa error

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S15_ManajemenPelanggan/'
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'
String keyword = 'zzzzz'

TestObject kolomCari = obj("//input[contains(@placeholder,'Cari nama')]")

def ambilNama = {
	return WebUI.executeJavaScript('''
return Array.from(document.querySelectorAll('table tbody tr')).filter(function(r) {
	return r.querySelectorAll('td').length > 1;
}).map(function(r) { return r.querySelectorAll('td')[1].innerText.trim(); });
''', null)
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

// ===== Precondition: Admin sudah login =====
WebUI.navigateToUrl(base + '/login')
WebUI.delay(2)
WebUI.setText(obj("(//input[@type='email' or @name='email'])[1]"), adminEmail)
WebUI.setText(obj("(//input[@type='password'])[1]"), adminPass)
WebUI.click(obj("(//button[@type='submit'])[1]"))
WebUI.delay(3)
if (WebUI.getUrl().contains('/login')) {
	KeywordUtil.markFailedAndStop('Login admin gagal. Periksa email dan password admin di script.')
}

// ===== Langkah 1: Buka menu Pelanggan =====
if (WebUI.verifyElementPresent(obj("//a[contains(@href,'/admin/pelanggan')]"), 5, FailureHandling.OPTIONAL)) {
	WebUI.click(obj("//a[contains(@href,'/admin/pelanggan')]"))
} else {
	WebUI.navigateToUrl(base + '/admin/pelanggan')
}
WebUI.delay(2)
int jumlahAwal = ambilNama().size()
WebUI.takeScreenshot(shot + 'TC-15.3_Gambar1_sebelum_cari.png')

// ===== Langkah 2: Ketik kata kunci yang tidak ada =====
WebUI.click(kolomCari)
WebUI.sendKeys(kolomCari, keyword)
WebUI.sendKeys(kolomCari, Keys.chord(Keys.ENTER))
WebUI.delay(2)
WebUI.takeScreenshot(shot + 'TC-15.3_Gambar2_hasil_cari_zzzzz.png')

// ===== Verifikasi =====
WebUI.verifyEqual(ambilNama().size(), 0, FailureHandling.CONTINUE_ON_FAILURE)     // tidak ada baris data

String bodyText = WebUI.getText(obj('//body')).toLowerCase()
boolean adaPesan = bodyText.contains('tidak ditemukan') || bodyText.contains('tidak ada data') ||
		bodyText.contains('belum ada') || bodyText.contains('data kosong') || bodyText.contains('tidak ada pelanggan')
boolean adaError = bodyText.contains('whoops') || bodyText.contains('exception') ||
		bodyText.contains('server error') || bodyText.contains('sqlstate')
def footer = (bodyText =~ /menampilkan [0-9]+ dari [0-9]+ pelanggan/)
KeywordUtil.logInfo("Pesan 'tidak ditemukan' tampil: ${adaPesan} | Error halaman: ${adaError} | Footer: ${footer.find() ? footer.group() : '(tidak ada)'}")

WebUI.verifyEqual(adaPesan, true, FailureHandling.CONTINUE_ON_FAILURE)    // FAIL = tabel kosong tanpa pesan
WebUI.verifyEqual(adaError, false, FailureHandling.CONTINUE_ON_FAILURE)

// ===== Hapus kata kunci: daftar kembali normal =====
WebUI.click(kolomCari)
WebUI.sendKeys(kolomCari, Keys.chord(Keys.CONTROL, 'a'))
WebUI.sendKeys(kolomCari, Keys.chord(Keys.BACK_SPACE))
WebUI.sendKeys(kolomCari, Keys.chord(Keys.ENTER))
WebUI.delay(2)
WebUI.verifyEqual(ambilNama().size(), jumlahAwal, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-15.3_Gambar3_setelah_hapus_kata_kunci.png')

WebUI.closeBrowser()