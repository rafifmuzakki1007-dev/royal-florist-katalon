import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S19_ManajemenStaff/'

// Hasil: [kartuTotal, kartuAktif, kartuNonaktif, barisTabel, barisAktif, barisNonaktif,
//         barisDenganAksi, angkaMenampilkan, header, barisSelKosong]
String jsCek = '''
var lines = document.body.innerText.split('\\n').map(function(l) { return l.trim(); }).filter(function(l) { return l !== ''; });
function kartu(label) {
	var i = lines.indexOf(label);
	if (i < 0) { return -1; }
	var v = parseInt(lines[i + 1]);
	return isNaN(v) ? -1 : v;
}
var tabel = Array.from(document.querySelectorAll('table')).filter(function(t) {
	return t.innerText.indexOf('Nama Staff') >= 0;
})[0];
if (!tabel) { return ['TABEL TIDAK ADA']; }
var heads = Array.from(tabel.querySelectorAll('thead th')).map(function(h) { return h.innerText.trim().toLowerCase(); });
var iStatus = heads.indexOf('status');
var rows = Array.from(tabel.querySelectorAll('tbody tr')).filter(function(r) { return r.querySelectorAll('td').length > 1; });
var aktif = 0, non = 0, aksiOk = 0, kosong = 0;
rows.forEach(function(r) {
	var td = r.querySelectorAll('td');
	var s = iStatus >= 0 ? td[iStatus].innerText.trim().toLowerCase() : '';
	if (s === 'aktif') { aktif++; } else if (s === 'nonaktif') { non++; }
	if (td[td.length - 1].querySelectorAll('a,button').length >= 3) { aksiOk++; }
	for (var k = 0; k < td.length - 1; k++) { if (td[k].innerText.trim() === '') { kosong++; } }
});
var m = document.body.innerText.match(/Menampilkan\\s+([0-9]+)\\s+dari\\s+([0-9]+)/);
return [kartu('Total Staff'), kartu('Staff Aktif'), kartu('Staff Nonaktif'), rows.length, aktif, non, aksiOk,
	m ? parseInt(m[2]) : -1, heads.join('|'), kosong];
'''

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
	// ===== Precondition: Admin sudah login =====
	WebUI.navigateToUrl(base + '/login')
	WebUI.delay(2)
	WebUI.setText(obj("(//input[@type='email' or @name='email'])[1]"), adminEmail)
	WebUI.setText(obj("(//input[@type='password'])[1]"), adminPass)
	WebUI.click(obj("(//button[@type='submit'])[1]"))
	WebUI.delay(3)
	WebUI.takeScreenshot(shot + 'TC-19.1_Gambar0_setelah_login.png')
	if (WebUI.getUrl().contains('/login')) {
		KeywordUtil.markFailedAndStop('Login admin gagal. Periksa email dan password admin di script.')
	}

	// ===== Langkah 1: Buka menu Manajemen Staff =====
	String menuXp = "//a[contains(@href,'/admin/karyawan')]"
	if (WebUI.verifyElementPresent(obj(menuXp), 5, FailureHandling.OPTIONAL)) {
		WebUI.click(obj(menuXp))
	} else {
		KeywordUtil.logInfo('Menu Manajemen Staff tidak ditemukan, membuka lewat URL langsung')
		WebUI.navigateToUrl(base + '/admin/karyawan')
	}
	WebUI.delay(2)
	WebUI.verifyMatch(WebUI.getUrl(), '.*admin/karyawan.*', true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.takeScreenshot(shot + 'TC-19.1_Gambar1_menu_manajemen_staff.png')

	// ===== Langkah 2: Amati kartu statistik dan tabel Daftar Staff =====
	for (t in ['Manajemen Staff', 'Total Staff', 'Staff Aktif', 'Staff Nonaktif', 'Daftar Staff', 'Tambah Staff']) {
		WebUI.verifyTextPresent(t, false, FailureHandling.CONTINUE_ON_FAILURE)
	}
	WebUI.takeScreenshot(shot + 'TC-19.1_Gambar2_kartu_statistik.png')
	WebUI.scrollToElement(obj("//*[normalize-space()='Daftar Staff']"), 5, FailureHandling.OPTIONAL)
	WebUI.delay(1)
	WebUI.takeScreenshot(shot + 'TC-19.1_Gambar3_tabel_daftar_staff.png')

	def cek = WebUI.executeJavaScript(jsCek, null)
	if (cek.size() < 10) {
		KeywordUtil.markFailedAndStop('Tabel Daftar Staff tidak ditemukan: ' + cek)
	}
	KeywordUtil.logInfo("KARTU: total=${cek[0]} aktif=${cek[1]} nonaktif=${cek[2]} | TABEL: baris=${cek[3]} aktif=${cek[4]} nonaktif=${cek[5]} " +
		"barisDenganAksi=${cek[6]} angkaMenampilkan=${cek[7]} selKosong=${cek[9]}")
	KeywordUtil.logInfo('HEADER TABEL: ' + cek[8])

	// Data lengkap: kartu sama dengan isi tabel
	WebUI.verifyEqual(cek[0] as int, cek[3] as int, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(cek[1] as int, cek[4] as int, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(cek[2] as int, cek[5] as int, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(cek[7] as int, cek[3] as int, FailureHandling.CONTINUE_ON_FAILURE)
	// Kolom lengkap
	for (h in ['no', 'nama staff', 'email', 'role', 'no. telepon', 'status', 'aksi']) {
		WebUI.verifyEqual(cek[8].toString().contains(h), true, FailureHandling.CONTINUE_ON_FAILURE)
	}
	// Setiap baris punya data (tidak ada sel kosong) dan ikon aksi (lihat, edit, hapus)
	WebUI.verifyEqual(cek[9] as int, 0, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(cek[6] as int, cek[3] as int, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual((cek[3] as int) > 0, true, FailureHandling.CONTINUE_ON_FAILURE)
} catch (Exception e) {
	WebUI.takeScreenshot(shot + 'TC-19.1_ERROR.png')
	KeywordUtil.markFailed('TC-19.1 berhenti: ' + e.getMessage())
} finally {
	WebUI.closeBrowser()
}