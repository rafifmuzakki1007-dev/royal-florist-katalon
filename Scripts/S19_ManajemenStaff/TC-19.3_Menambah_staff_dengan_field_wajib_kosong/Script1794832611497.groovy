import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

// TC-19.3 : Menambah staff dengan field wajib kosong (Negative)
// Nama dan username dikosongkan. Pass = sistem menolak (staff TIDAK tersimpan).

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S19_ManajemenStaff/'
String stamp = String.valueOf(System.currentTimeMillis())

// [totalKartu, jumlahBaris, teksTabel]
String jsList = '''
var lines = document.body.innerText.split('\\n').map(function(l) { return l.trim(); }).filter(function(l) { return l !== ''; });
var i = lines.indexOf('Total Staff');
var total = i >= 0 ? parseInt(lines[i + 1]) : -1;
var tabel = Array.from(document.querySelectorAll('table')).filter(function(t) {
	return t.innerText.indexOf('Nama Staff') >= 0;
})[0];
var rows = tabel ? Array.from(tabel.querySelectorAll('tbody tr')).filter(function(r) { return r.querySelectorAll('td').length > 1; }) : [];
return [isNaN(total) ? -1 : total, rows.length, tabel ? tabel.innerText.replace(/\\s+/g, ' ') : ''];
'''

// Tandai isian form (di dalam form yang memuat kolom password) dan tombol simpan
String jsForm = '''
function vis(e) { return !!(e.offsetWidth || e.offsetHeight || e.getClientRects().length); }
var pw = Array.from(document.querySelectorAll('input[type=password]')).filter(vis)[0];
var scope = pw ? (pw.closest('form') || document) : document;
function label(e) {
	var t = '';
	if (e.id) { var l = document.querySelector('label[for="' + e.id + '"]'); if (l) { t += ' ' + l.innerText; } }
	var p = e.closest('label'); if (p) { t += ' ' + p.innerText; }
	var prev = e.previousElementSibling; if (prev && prev.tagName === 'LABEL') { t += ' ' + prev.innerText; }
	var par = e.parentElement; if (par) { var l2 = par.querySelector('label'); if (l2) { t += ' ' + l2.innerText; } }
	return t;
}
var hasil = []; var idx = 0;
Array.from(scope.querySelectorAll('input,select,textarea')).forEach(function(e) {
	if (!vis(e) || e.disabled || e.readOnly) { return; }
	var tipe = (e.type || '').toLowerCase();
	if (['hidden', 'submit', 'button', 'checkbox', 'radio', 'file', 'search'].indexOf(tipe) >= 0) { return; }
	var h = ((e.name || '') + ' ' + (e.id || '') + ' ' + (e.placeholder || '') + ' ' + label(e)).toLowerCase();
	if (h.indexOf('cari') >= 0) { return; }
	var kind = 'lain';
	if (tipe === 'password') { kind = 'password'; }
	else if (e.tagName === 'SELECT') {
		if (/role|peran|jabatan/.test(h)) { kind = 'role'; } else if (/status/.test(h)) { kind = 'status'; } else { kind = 'select'; }
	}
	else if (tipe === 'email' || h.indexOf('email') >= 0) { kind = 'email'; }
	else if (tipe === 'tel' || /telp|telepon|phone|hp|whatsapp/.test(h)) { kind = 'telp'; }
	else if (/username|user name|pengguna/.test(h)) { kind = 'username'; }
	else if (h.indexOf('nama') >= 0) { kind = 'nama'; }
	else if (e.tagName === 'TEXTAREA' || /alamat/.test(h)) { kind = 'alamat'; }
	e.setAttribute('data-kt-i', idx);
	hasil.push(idx + '|' + kind + '|' + tipe + '|' + (e.name || '') + '|' + (e.placeholder || '') + '|required=' + e.required + '|' + label(e).replace(/\\s+/g, ' ').trim());
	idx++;
});
var tombol = null;
var btns = Array.from(scope.querySelectorAll('button,input[type=submit]')).filter(vis);
for (var k = 0; k < btns.length; k++) {
	if (/simpan|tambah|save|submit/i.test((btns[k].innerText || '') + ' ' + (btns[k].value || ''))) { tombol = btns[k]; break; }
}
if (!tombol) { for (var m = 0; m < btns.length; m++) { if (btns[m].type === 'submit') { tombol = btns[m]; break; } } }
if (tombol) { tombol.setAttribute('data-kt-submit', 'ya'); }
return hasil.join('\\n') + '\\nTOMBOL=' + (tombol ? (tombol.innerText || tombol.value).trim() : 'TIDAK ADA');
'''

// Pilih opsi pada select: cocok persis dulu, lalu mengandung teks, lalu opsi pertama
String jsPilih = '''
var e = document.querySelector('[data-kt-i="' + arguments[0] + '"]');
var pref = arguments[1].toLowerCase();
var pilih = -1;
for (var i = 0; i < e.options.length; i++) {
	if (e.options[i].value !== '' && e.options[i].text.trim().toLowerCase() === pref) { pilih = i; break; }
}
if (pilih < 0) {
	for (var j = 0; j < e.options.length; j++) {
		if (e.options[j].value !== '' && e.options[j].text.toLowerCase().indexOf(pref) >= 0) { pilih = j; break; }
	}
}
if (pilih < 0) {
	for (var k = 0; k < e.options.length; k++) { if (e.options[k].value !== '') { pilih = k; break; } }
}
if (pilih >= 0) { e.selectedIndex = pilih; e.dispatchEvent(new Event('change', {bubbles: true})); }
return e.options[e.selectedIndex] ? e.options[e.selectedIndex].text : '';
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
	if (WebUI.getUrl().contains('/login')) {
		KeywordUtil.markFailedAndStop('Login admin gagal. Periksa email dan password admin di script.')
	}

	// ===== Langkah 1: Buka menu Manajemen Staff =====
	String menuXp = "//a[contains(@href,'/admin/karyawan')]"
	if (WebUI.verifyElementPresent(obj(menuXp), 5, FailureHandling.OPTIONAL)) {
		WebUI.click(obj(menuXp))
	} else {
		WebUI.navigateToUrl(base + '/admin/karyawan')
	}
	WebUI.delay(2)
	def sebelum = WebUI.executeJavaScript(jsList, null)
	int totalSebelum = sebelum[0] as int
	int barisSebelum = sebelum[1] as int
	KeywordUtil.logInfo("SEBELUM: kartuTotal=${totalSebelum} barisTabel=${barisSebelum}")
	WebUI.takeScreenshot(shot + 'TC-19.3_Gambar1_daftar_sebelum.png')

	// Data uji: nama dan username KOSONG, isian lain valid dan unik
	String emailBaru = 'tanpa.nama' + stamp + '@gmail.com'
	Map nilai = [
		nama    : '',
		email   : emailBaru,
		telp    : '08' + stamp.substring(4),
		username: '',
		password: 'Staff#2026',
		alamat  : 'Jl. Melati No. 12, Nganjuk',
		lain    : 'Staff'
	]
	KeywordUtil.logInfo('DATA UJI: ' + nilai)

	// ===== Langkah 2: Klik tombol Tambah Staff =====
	String tambahXp = "//*[(self::a or self::button) and contains(normalize-space(.),'Tambah Staff')]"
	if (!WebUI.verifyElementPresent(obj(tambahXp), 5, FailureHandling.OPTIONAL)) {
		KeywordUtil.markFailedAndStop('Tombol Tambah Staff tidak ditemukan.')
	}
	WebUI.click(obj(tambahXp))
	WebUI.delay(2)
	WebUI.takeScreenshot(shot + 'TC-19.3_Gambar2_form_tambah_staff.png')

	// ===== Langkah 3: Kosongkan nama dan username (isian lain diisi valid) =====
	String info = WebUI.executeJavaScript(jsForm, null).toString()
	KeywordUtil.logInfo('ISIAN FORM TAMBAH STAFF:\n' + info)
	def daftarField = info.split('\n').findAll { it.matches(/^[0-9]+\|.*/) }
	if (daftarField.isEmpty()) {
		KeywordUtil.markFailedAndStop('Form Tambah Staff tidak terdeteksi. Kirim log ISIAN FORM dan screenshot Gambar2.')
	}
	if (info.contains('TOMBOL=TIDAK ADA')) {
		KeywordUtil.markFailedAndStop('Tombol Simpan pada form tidak terdeteksi. Kirim log ISIAN FORM dan screenshot Gambar2.')
	}
	daftarField.each { String baris ->
		def p = baris.split('\\|')
		String idx = p[0]
		String kind = p[1]
		if (kind == 'role') {
			KeywordUtil.logInfo('Role dipilih: ' + WebUI.executeJavaScript(jsPilih, [idx, 'staff']))
		} else if (kind == 'status') {
			KeywordUtil.logInfo('Status dipilih: ' + WebUI.executeJavaScript(jsPilih, [idx, 'aktif']))
		} else if (kind == 'select') {
			KeywordUtil.logInfo('Pilihan lain: ' + WebUI.executeJavaScript(jsPilih, [idx, '']))
		} else {
			String v = nilai.containsKey(kind) ? nilai[kind] : ''
			if (v != '') {
				WebUI.setText(obj("//*[@data-kt-i='" + idx + "']"), v)
			}
		}
	}
	WebUI.delay(1)
	WebUI.takeScreenshot(shot + 'TC-19.3_Gambar3_nama_username_kosong.png')

	// ===== Langkah 4: Klik tombol Simpan =====
	String teksSebelum = WebUI.executeJavaScript('return document.body.innerText', null).toString()
	WebUI.click(obj("//*[@data-kt-submit='ya']"))
	WebUI.delay(1)
	String alertTeks = ''
	if (WebUI.verifyAlertPresent(2, FailureHandling.OPTIONAL)) {
		alertTeks = WebUI.getAlertText(FailureHandling.OPTIONAL)
		KeywordUtil.logInfo('ALERT: ' + alertTeks)
		WebUI.acceptAlert(FailureHandling.OPTIONAL)
	}
	// Pesan validasi bawaan browser (field required) dan teks baru dari server
	String validasiBrowser = WebUI.executeJavaScript('''
return Array.from(document.querySelectorAll('[data-kt-i]')).filter(function(e) { return !e.checkValidity(); })
	.map(function(e) { return (e.name || e.id) + ': ' + e.validationMessage; }).join(' ; ');
''', null).toString()
	String teksSesudah = WebUI.executeJavaScript('return document.body.innerText', null).toString()
	def teksBaru = teksSesudah.split('\n').collect { it.trim() }.findAll { it && !teksSebelum.contains(it) }
	KeywordUtil.logInfo('VALIDASI BROWSER: ' + validasiBrowser)
	KeywordUtil.logInfo('TEKS BARU SETELAH SIMPAN: ' + teksBaru)
	KeywordUtil.logInfo('URL SESUDAH SIMPAN: ' + WebUI.getUrl())
	WebUI.takeScreenshot(shot + 'TC-19.3_Gambar4_setelah_simpan.png')
	if (validasiBrowser == '' && teksBaru.isEmpty() && alertTeks == '') {
		KeywordUtil.markWarning('Tidak terdeteksi pesan validasi. Periksa Gambar4 secara visual.')
	}

	// ===== Verifikasi: staff TIDAK tersimpan =====
	WebUI.navigateToUrl(base + '/admin/karyawan')
	WebUI.delay(2)
	def sesudah = WebUI.executeJavaScript(jsList, null)
	KeywordUtil.logInfo("SESUDAH: kartuTotal=${sesudah[0]} barisTabel=${sesudah[1]}")
	WebUI.scrollToElement(obj("//*[normalize-space()='Daftar Staff']"), 5, FailureHandling.OPTIONAL)
	WebUI.delay(1)
	WebUI.takeScreenshot(shot + 'TC-19.3_Gambar5_daftar_sesudah.png')
	WebUI.verifyEqual(sesudah[1] as int, barisSebelum, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(sesudah[0] as int, totalSebelum, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(sesudah[2].toString().contains(emailBaru), false, FailureHandling.CONTINUE_ON_FAILURE)
} catch (Exception e) {
	WebUI.takeScreenshot(shot + 'TC-19.3_ERROR.png')
	KeywordUtil.markFailed('TC-19.3 berhenti: ' + e.getMessage())
} finally {
	WebUI.closeBrowser()
}