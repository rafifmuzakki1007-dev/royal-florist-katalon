import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil
import org.openqa.selenium.Keys

// TC-18.3 : Memperbarui stok dengan nilai negatif (Negative)
// Data    : Buket Mawar Merah (#PRD-001); Jumlah Stok Baru = -5
// PASS jika -5 ditolak dan stok tidak berubah. FAIL (bug) jika -5 tersimpan.

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S18_ManajemenStok/'
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'

String sku     = '#PRD-001'
String stokUji = '-5'

TestObject stokCell   = obj("//tr[contains(.,'" + sku + "')]/td[6]")
TestObject btnEdit    = obj("(//tr[contains(.,'" + sku + "')]/td[last()]//*[self::button or self::a])[1]")
TestObject modalTitle = obj("//*[normalize-space()='Penyesuaian Stok']")
TestObject inputStok  = obj("//label[normalize-space()='Jumlah Stok Baru']/following::input[@type='number'][1]")
TestObject btnSimpan  = obj("//button[normalize-space()='Simpan Stok']")
TestObject btnBatal   = obj("//button[normalize-space()='Batal']")

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

// ===== Langkah 1: Buka menu Stok =====
if (WebUI.verifyElementPresent(obj("//a[contains(@href,'/admin/stok')]"), 5, FailureHandling.OPTIONAL)) {
	WebUI.click(obj("//a[contains(@href,'/admin/stok')]"))
} else {
	WebUI.navigateToUrl(base + '/admin/stok')
}
WebUI.delay(2)
WebUI.scrollToElement(stokCell, 5, FailureHandling.OPTIONAL)
String stokAwal = WebUI.getText(stokCell).trim()
KeywordUtil.logInfo("Stok awal ${sku} = ${stokAwal}")
WebUI.takeScreenshot(shot + 'TC-18.3_Gambar1_stok_sebelum.png')

// ===== Langkah 2: Klik ikon edit =====
WebUI.click(btnEdit)
WebUI.delay(1)
WebUI.verifyElementVisible(modalTitle, FailureHandling.CONTINUE_ON_FAILURE)

// ===== Langkah 3: Isi stok dengan nilai negatif (diketik, bukan lewat spinner) =====
WebUI.click(inputStok)
WebUI.sendKeys(inputStok, Keys.chord(Keys.CONTROL, 'a'))
WebUI.sendKeys(inputStok, stokUji)
WebUI.takeScreenshot(shot + 'TC-18.3_Gambar2_input_minus5.png')

// ===== Langkah 4: Klik Simpan Stok =====
WebUI.click(btnSimpan)
WebUI.delay(2)
WebUI.takeScreenshot(shot + 'TC-18.3_Gambar3_setelah_klik_simpan.png')   // bukti pesan penolakan / modal tetap terbuka

String bodyText = WebUI.getText(obj('//body')).toLowerCase()
boolean adaPesan = bodyText.contains('negatif') || bodyText.contains('tidak boleh') || bodyText.contains('minimal') || bodyText.contains('minimum')
KeywordUtil.logInfo('Pesan penolakan terdeteksi di halaman: ' + adaPesan + ' (validasi bawaan browser tidak terbaca, cek screenshot)')

if (WebUI.verifyElementPresent(btnBatal, 1, FailureHandling.OPTIONAL)) {
	WebUI.click(btnBatal)
}

// ===== Verifikasi: stok TIDAK berubah, juga setelah refresh =====
WebUI.refresh()
WebUI.delay(2)
WebUI.scrollToElement(stokCell, 5, FailureHandling.OPTIONAL)
String stokAkhir = WebUI.getText(stokCell).trim()
WebUI.takeScreenshot(shot + 'TC-18.3_Gambar4_setelah_refresh.png')
KeywordUtil.logInfo("Stok akhir ${sku} = ${stokAkhir}")

WebUI.closeBrowser()

if (stokAkhir == stokUji) {
	KeywordUtil.markFailed("TC-18.3 FAIL (BUG): stok berubah menjadi ${stokUji}, sistem menerima nilai negatif.")
} else if (stokAkhir != stokAwal) {
	KeywordUtil.markFailed("TC-18.3 FAIL: stok berubah dari ${stokAwal} menjadi ${stokAkhir}.")
} else {
	KeywordUtil.markPassed("TC-18.3 PASS: nilai ${stokUji} ditolak, stok tetap ${stokAkhir}.")
}