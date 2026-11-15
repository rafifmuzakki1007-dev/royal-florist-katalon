import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil
import org.openqa.selenium.Keys

// TC-15.2 : Mencari pelanggan menggunakan kolom pencarian (Positive)
// Data    : Kata kunci "Rina"
// Data uji saat ini (10 pelanggan): nama yang mengandung "rina" ada 2 -> "rina" dan "Rina Oktavia".
// Nama mirip yang TIDAK boleh muncul: rima, rani, ria, romi, romlah, parmi, karmi, roi.

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S15_ManajemenPelanggan/'
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'

String keyword = 'Rina'
int expectedJumlah = 2                                   // ubah bila data pelanggan berubah
List<String> tidakBolehMuncul = ['rima@gmail.com', 'rani@gmail.com', 'ria@gmail.com', 'parmi@gmail.com']

TestObject kolomCari = obj("//input[contains(@placeholder,'Cari nama')]")

// Ambil daftar nama pada baris tabel yang sedang tampil
def ambilNama = {
	return WebUI.executeJavaScript('''
return Array.from(document.querySelectorAll('table tbody tr')).filter(function(r) {
	return r.querySelectorAll('td').length > 1;
}).map(function(r) { return r.querySelectorAll('td')[1].innerText.trim(); });
''', null)
}

// Ketik kata kunci di kolom pencarian
def cari = { String kata ->
	WebUI.click(kolomCari)
	WebUI.sendKeys(kolomCari, Keys.chord(Keys.CONTROL, 'a'))
	WebUI.sendKeys(kolomCari, Keys.chord(Keys.BACK_SPACE))
	if (kata != '') {
		WebUI.sendKeys(kolomCari, kata)
	}
	WebUI.sendKeys(kolomCari, Keys.chord(Keys.ENTER))      // aman juga bila pencarian bersifat live
	WebUI.delay(2)
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
def semuaNama = ambilNama()
KeywordUtil.logInfo("Sebelum cari: ${semuaNama.size()} pelanggan -> ${semuaNama}")
WebUI.takeScreenshot(shot + 'TC-15.2_Gambar1_sebelum_cari.png')

// ===== Langkah 2: Ketik nama pelanggan pada kolom pencarian =====
cari(keyword)
WebUI.takeScreenshot(shot + 'TC-15.2_Gambar2_hasil_cari_rina.png')

// ===== Verifikasi: hanya pelanggan yang namanya mengandung kata kunci =====
def hasil = ambilNama()
KeywordUtil.logInfo("Hasil cari '${keyword}': ${hasil.size()} pelanggan -> ${hasil}")
WebUI.verifyEqual(hasil.size(), expectedJumlah, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(hasil.every { it.toLowerCase().contains(keyword.toLowerCase()) }, true, FailureHandling.CONTINUE_ON_FAILURE)
tidakBolehMuncul.each { WebUI.verifyTextNotPresent(it, false, FailureHandling.CONTINUE_ON_FAILURE) }   // nama mirip tersaring

// ===== Tambahan: pencarian tidak peka huruf besar/kecil =====
cari(keyword.toUpperCase())
def hasilKapital = ambilNama()
KeywordUtil.logInfo("Hasil cari '${keyword.toUpperCase()}': ${hasilKapital.size()} pelanggan -> ${hasilKapital}")
WebUI.verifyEqual(hasilKapital.size(), expectedJumlah, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-15.2_Gambar3_hasil_cari_RINA_kapital.png')

// ===== Hapus kata kunci: daftar kembali lengkap =====
cari('')
WebUI.verifyEqual(ambilNama().size(), semuaNama.size(), FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-15.2_Gambar4_setelah_hapus_kata_kunci.png')

WebUI.closeBrowser()