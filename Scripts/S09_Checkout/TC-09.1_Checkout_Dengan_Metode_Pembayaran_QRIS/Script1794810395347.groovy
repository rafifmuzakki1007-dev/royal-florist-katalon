import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

// TC-09.1 : Checkout dengan metode pembayaran QRIS (Positive)
// Fokus: modul Checkout. Isi halaman Pembayaran (S10) hanya dicatat, tidak menentukan Passed/Failed.
// Skrip menyiapkan sendiri keranjang dan alamat bila belum ada. Bila otomatis gagal,
// skrip MENUNGGU kamu melengkapinya manual di jendela browser (maks. 90 detik), tidak langsung error.

// ===================== KONFIGURASI =====================
String base = 'http://127.0.0.1:8000'
String emailPelanggan = 'parmi@gmail.com'
String passPelanggan  = '12345678'
String alamatText    = 'Jl. Melati No. 12'
String alamatLengkap = 'Jl. Melati No. 12, Nganjuk'
int tungguManualDetik = 90
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S09_Checkout/'
// =======================================================

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

def rupiah = { int n -> n.toString().reverse().replaceAll(/(\d{3})(?=\d)/, '$1.').reverse() }

// Klik elemen bila ada (tidak error bila tidak ada). Hasil: true bila diklik
def klik = { String xp ->
	if (WebUI.verifyElementPresent(obj(xp), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(obj(xp), FailureHandling.OPTIONAL)
		return true
	}
	return false
}

def teksHalaman = { -> WebUI.getText(obj('//body')) }

// Status pilihan radio: cari kartu terdekat yang memuat teks dan hanya berisi 1 radio
// Hasil: TERPILIH / TIDAK_TERPILIH / TIDAK_ADA_RADIO
String jsPilihan = '''
var teks = arguments[0];
var rs = Array.from(document.querySelectorAll('input[type=radio]')).filter(function(e) {
	var n = e;
	for (var i = 0; i < 6 && n; i++) {
		if (n.innerText && n.innerText.length < 250 && n.innerText.indexOf(teks) >= 0
			&& n.querySelectorAll('input[type=radio]').length === 1) { return true; }
		n = n.parentElement;
	}
	return false;
});
if (rs.length === 0) { return 'TIDAK_ADA_RADIO'; }
return rs[0].checked ? 'TERPILIH' : 'TIDAK_TERPILIH';
'''
def statusPilihan = { String t -> WebUI.executeJavaScript(jsPilihan, [t]) }

// Teks pada tombol utama checkout (contoh: "Lanjut Bayar QRIS →")
def teksTombolBayar = {
	return WebUI.executeJavaScript("var b = document.getElementById('cta'); return b ? b.innerText.trim() : ''", null)
}

// Tunggu sampai kondisi terpenuhi (untuk bantuan manual)
def tunggu = { Closure kondisi, String pesan ->
	KeywordUtil.logInfo(pesan)
	for (int i = 0; i < tungguManualDetik; i += 3) {
		if (kondisi()) { return true }
		WebUI.delay(3)
	}
	return kondisi()
}

def loginPelanggan = {
	WebUI.navigateToUrl(base + '/login')
	WebUI.waitForPageLoad(15)
	WebUI.setText(obj("(//input[@type='email' or @name='email'])[1]"), emailPelanggan)
	WebUI.setText(obj("(//input[@type='password'])[1]"), passPelanggan)
	WebUI.click(obj("(//button[@type='submit'])[1]"))
	WebUI.waitForPageLoad(15)
	WebUI.delay(2)
	if (WebUI.getUrl().contains('/login')) {
		WebUI.takeScreenshot(shot + 'TC-09.1_LOGIN_gagal.png')
		KeywordUtil.markFailedAndStop('Login gagal untuk ' + emailPelanggan + '. Pastikan password akun ini 12345678.')
	}
}

def bukaCheckout = {
	if (!WebUI.getUrl().contains('checkout')) {
		if (klik("//a[contains(@href,'keranjang') or contains(@href,'cart')]")) {
			WebUI.delay(2)
			klik("//*[self::a or self::button][contains(normalize-space(),'Checkout')]")
			WebUI.delay(2)
		}
	}
	if (!WebUI.getUrl().contains('checkout')) {
		WebUI.navigateToUrl(base + '/pelanggan/checkout')
		WebUI.delay(2)
	}
}

// Ringkasan Tagihan: [subtotal, biayaKurir, total] (-1 bila tidak terbaca)
def ringkasan = {
	return WebUI.executeJavaScript('''
var lines = document.body.innerText.split('\\n').map(function(l) { return l.trim(); }).filter(function(l) { return l !== ''; });
function nilai(label) {
	for (var i = 0; i < lines.length; i++) {
		if (lines[i].indexOf(label) === 0) {
			for (var j = i; j <= i + 2 && j < lines.length; j++) {
				if (lines[j].indexOf('Rp') >= 0) { return parseInt(lines[j].replace(/[^0-9]/g, '')); }
			}
		}
	}
	return -1;
}
return [nilai('Subtotal Produk'), nilai('Biaya Kurir'), nilai('Total Wajib Bayar')];
''', null)
}

// Buka Riwayat Pesanan dan kembalikan teks halamannya ('' bila link tidak ditemukan)
def bukaRiwayat = {
	if (!klik("//a[normalize-space()='Riwayat Pesanan']")) { return '' }
	WebUI.delay(2)
	return teksHalaman()
}

// Klik tombol Lanjut Bayar: scroll dulu, lalu klik biasa, cadangan klik via JavaScript
def klikLanjutBayar = {
	String xp = "//button[@id='cta'] | //*[self::button or self::a][contains(normalize-space(),'Lanjut Bayar')]"
	WebUI.scrollToElement(obj(xp), 5, FailureHandling.OPTIONAL)
	WebUI.delay(1)
	KeywordUtil.logInfo('Tombol Lanjut Bayar: ' + WebUI.executeJavaScript(
		"var b = document.getElementById('cta'); return b ? ('disabled=' + b.disabled + ' text=' + b.innerText.trim()) : 'tombol #cta tidak ada'", null))
	String urlSebelum = WebUI.getUrl()
	WebUI.click(obj(xp), FailureHandling.OPTIONAL)
	WebUI.delay(3)
	if (WebUI.getUrl() == urlSebelum) {
		KeywordUtil.logInfo('Klik biasa tidak berpindah halaman, mencoba klik via JavaScript')
		WebUI.executeJavaScript("var b = document.getElementById('cta'); if (b) { b.click(); }", null)
		WebUI.delay(3)
	}
}

// Isi form alamat baru secara otomatis (mengisi semua kolom yang kosong sesuai petunjuknya)
def tambahAlamatOtomatis = {
	try {
		if (!klik("//*[self::button or self::a][contains(normalize-space(),'Tambah Alamat Baru')]")) { return false }
		WebUI.delay(2)
		WebUI.takeScreenshot(shot + 'TC-09.1_Gambar0_form_alamat.png')
		def terisi = WebUI.executeJavaScript('''
var alamat = arguments[0];
function terlihat(e) { return !!(e.offsetWidth || e.offsetHeight || e.getClientRects().length); }
function petunjuk(e) {
	var t = (e.name || '') + ' ' + (e.id || '') + ' ' + (e.placeholder || '');
	var lab = e.id ? document.querySelector('label[for="' + e.id + '"]') : null;
	if (lab) { t += ' ' + lab.innerText; }
	var p = e.closest('label');
	if (p) { t += ' ' + p.innerText; }
	return t.toLowerCase();
}
function setNilai(e, v) {
	var proto = e.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;
	Object.getOwnPropertyDescriptor(proto, 'value').set.call(e, v);
	e.dispatchEvent(new Event('input', {bubbles: true}));
	e.dispatchEvent(new Event('change', {bubbles: true}));
}
var hasil = [];
document.querySelectorAll('input, textarea, select').forEach(function(e) {
	if (!terlihat(e) || e.disabled || e.readOnly) { return; }
	var tipe = (e.type || '').toLowerCase();
	if (['hidden', 'submit', 'button', 'checkbox', 'radio', 'file', 'password', 'search'].indexOf(tipe) >= 0) { return; }
	var h = petunjuk(e);
	if (e.tagName === 'SELECT') {
		for (var i = 0; i < e.options.length; i++) {
			if (e.options[i].value !== '') { e.selectedIndex = i; break; }
		}
		e.dispatchEvent(new Event('change', {bubbles: true}));
		hasil.push('select:' + h.trim());
		return;
	}
	if (e.value && e.value.trim() !== '') { return; }
	var v;
	if (h.indexOf('telp') >= 0 || h.indexOf('hp') >= 0 || h.indexOf('phone') >= 0 || h.indexOf('whatsapp') >= 0 || tipe === 'tel') { v = '081234567890'; }
	else if (h.indexOf('pos') >= 0) { v = '64411'; }
	else if (h.indexOf('nama') >= 0 || h.indexOf('penerima') >= 0) { v = 'Rina Oktavia'; }
	else if (h.indexOf('provinsi') >= 0) { v = 'Jawa Timur'; }
	else if (h.indexOf('alamat') >= 0 || h.indexOf('jalan') >= 0 || h.indexOf('detail') >= 0 || e.tagName === 'TEXTAREA') { v = alamat; }
	else { v = 'Nganjuk'; }
	setNilai(e, v);
	hasil.push(h.trim() + ' = ' + v);
});
return hasil;
''', [alamatLengkap])
		KeywordUtil.logInfo('Kolom alamat terisi otomatis: ' + terisi)
		WebUI.takeScreenshot(shot + 'TC-09.1_Gambar0_form_alamat_terisi.png')
		klik("(//button[contains(normalize-space(),'Simpan')])[last()]")
		WebUI.delay(3)
		return !teksHalaman().contains('Belum ada alamat pengiriman')
	} catch (Exception e) {
		KeywordUtil.logInfo('Tambah alamat otomatis gagal: ' + e.getMessage())
		return false
	}
}

// Tambah produk dari Katalog (bila keranjang kosong)
def tambahProdukOtomatis = {
	try {
		if (!klik("//a[normalize-space()='Katalog']")) { return false }
		WebUI.delay(3)
		if (!klik("(//*[self::button or self::a][contains(normalize-space(),'Keranjang') or contains(normalize-space(),'Tambah')])[1]")) {
			klik("(//main//a[.//img])[1]")
			WebUI.delay(3)
		}
		klik("(//input[@type='radio'])[1]")
		klik("//*[self::button or self::a][contains(normalize-space(),'Tambah ke Keranjang')]")
		WebUI.delay(2)
		return true
	} catch (Exception e) {
		KeywordUtil.logInfo('Tambah produk otomatis gagal: ' + e.getMessage())
		return false
	}
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
	// ===== Precondition: pelanggan login =====
	loginPelanggan()

	// ===== Langkah 1: Buka halaman Checkout dari Keranjang =====
	bukaCheckout()
	def r = ringkasan()
	KeywordUtil.logInfo("Ringkasan awal: subtotal=${r[0]} biayaKurir=${r[1]} total=${r[2]}")
	// Keranjang kosong bila SUBTOTAL produk 0 (total tetap > 0 karena ada ongkir)
	if ((r[0] as int) <= 0) {
		KeywordUtil.logInfo('Keranjang kosong, menambahkan produk dari Katalog')
		tambahProdukOtomatis()
		bukaCheckout()
		r = ringkasan()
		if ((r[0] as int) <= 0) {
			WebUI.takeScreenshot(shot + 'TC-09.1_Gambar0_keranjang_kosong.png')
			boolean adaProduk = tunggu({ (ringkasan()[0] as int) > 0 },
				'Keranjang masih kosong. TAMBAHKAN PRODUK lalu buka halaman Checkout di jendela browser ini (skrip menunggu).')
			if (!adaProduk) {
				KeywordUtil.markFailedAndStop('Keranjang kosong dan produk tidak berhasil ditambahkan.')
			}
		}
	}

	if (teksHalaman().contains('Belum ada alamat pengiriman')) {
		KeywordUtil.logInfo('Belum ada alamat, menambahkan alamat')
		boolean alamatOk = tambahAlamatOtomatis()
		if (!alamatOk) {
			WebUI.takeScreenshot(shot + 'TC-09.1_Gambar0_belum_ada_alamat.png')
			alamatOk = tunggu({ !teksHalaman().contains('Belum ada alamat pengiriman') },
				'TAMBAHKAN ALAMAT di jendela browser ini (isi form lalu Simpan). Skrip menunggu.')
		}
		if (!alamatOk) {
			KeywordUtil.markFailedAndStop('Alamat pengiriman tidak berhasil ditambahkan.')
		}
		bukaCheckout()
	}
	WebUI.takeScreenshot(shot + 'TC-09.1_Gambar1_halaman_checkout.png')

	// ===== Langkah 2: Pilih alamat pengiriman =====
	boolean alamatDiklik = klik("//*[contains(normalize-space(text()),'" + alamatText + "')]")
	WebUI.delay(1)
	String stAlamat = statusPilihan(alamatText)
	KeywordUtil.logInfo("Alamat: diklik=${alamatDiklik} status=${stAlamat}")
	WebUI.verifyEqual(alamatDiklik, true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(stAlamat != 'TIDAK_TERPILIH', true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.takeScreenshot(shot + 'TC-09.1_Gambar2_alamat_dipilih.png')

	// ===== Langkah 3: Pilih metode pembayaran QRIS =====
	klik("//*[normalize-space(text())='Dikirim Kurir Florist']")
	klik("//*[normalize-space(text())='QRIS']")
	WebUI.delay(1)
	// Bukti QRIS terpilih: teks tombol berubah menjadi "Lanjut Bayar QRIS"
	String ctaTeks = teksTombolBayar()
	KeywordUtil.logInfo('Teks tombol bayar: ' + ctaTeks)
	WebUI.verifyEqual(ctaTeks.contains('QRIS'), true, FailureHandling.CONTINUE_ON_FAILURE)
	r = ringkasan()
	KeywordUtil.logInfo("Ringkasan: subtotal=${r[0]} biayaKurir=${r[1]} total=${r[2]}")
	WebUI.verifyEqual((r[0] as int) > 0, true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual((r[0] as int) + (r[1] as int), r[2] as int, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.takeScreenshot(shot + 'TC-09.1_Gambar3_metode_qris.png')

	// ===== Langkah 4: Klik Lanjut Bayar QRIS =====
	klikLanjutBayar()
	WebUI.delay(1)
	WebUI.takeScreenshot(shot + 'TC-09.1_Gambar4_halaman_pembayaran.png')

	// ===== Verifikasi 1: pesanan dibuat dan pindah ke halaman Pembayaran (ada id pesanan di URL) =====
	String urlSekarang = WebUI.getUrl()
	KeywordUtil.logInfo('URL setelah Lanjut Bayar: ' + urlSekarang)
	boolean masihDiForm = urlSekarang.endsWith('/pelanggan/checkout') || urlSekarang.endsWith('/pelanggan/checkout/')
	WebUI.verifyEqual(masihDiForm, false, FailureHandling.CONTINUE_ON_FAILURE)
	def mId = urlSekarang =~ /pembayaran\/(\d+)/
	String orderId = mId.find() ? mId.group(1) : ''
	KeywordUtil.logInfo('ID pesanan dari URL: ' + orderId)
	WebUI.verifyEqual(orderId != '', true, FailureHandling.CONTINUE_ON_FAILURE)

	// ===== Verifikasi 2: total pada halaman pembayaran sama dengan Ringkasan Tagihan =====
	String teks = teksHalaman().replaceAll(/\s/, '')
	WebUI.verifyEqual(teks.contains('Rp' + rupiah(r[2] as int)), true, FailureHandling.CONTINUE_ON_FAILURE)

	// ===== Catatan (tidak menentukan Passed/Failed): kode QR dan nomor invoice =====
	int elemenQr = WebUI.executeJavaScript('''
return Array.from(document.querySelectorAll('img,canvas,svg')).filter(function(e) {
	var b = e.getBoundingClientRect();
	return b.width >= 120 && b.height >= 120;
}).length;
''', null) as int
	KeywordUtil.logInfo('Elemen berukuran QR di halaman pembayaran: ' + elemenQr)
	if (elemenQr == 0) {
		KeywordUtil.markWarning('Kode QR tidak terdeteksi otomatis. Periksa Gambar4 secara visual (isi halaman Pembayaran adalah ruang lingkup S10).')
	}
	String invBaru = WebUI.executeJavaScript("var m = document.body.innerText.match(/INV-[A-Za-z0-9-]+/); return m ? m[0] : '';", null)
	KeywordUtil.logInfo('Nomor invoice pesanan baru: ' + (invBaru ? invBaru : '(belum tampil di halaman pembayaran)'))

	// ===== Verifikasi 3: pesanan baru tampil di Riwayat Pesanan =====
	String teksRiwayat = bukaRiwayat()
	WebUI.takeScreenshot(shot + 'TC-09.1_Gambar5_riwayat_pesanan.png')
	if (invBaru) {
		if (teksRiwayat.contains(invBaru)) {
			KeywordUtil.logInfo('Pesanan ' + invBaru + ' tampil di Riwayat Pesanan')
		} else {
			KeywordUtil.markWarning('Nomor ' + invBaru + ' tidak ditemukan di halaman 1 Riwayat Pesanan. Cek Gambar5 dan tabel orders (id ' + orderId + ') di phpMyAdmin.')
		}
	} else {
		KeywordUtil.markWarning('Nomor invoice belum tampil di halaman pembayaran. Cek Gambar5 dan tabel orders (id ' + orderId + ') di phpMyAdmin.')
	}
} catch (Exception e) {
	WebUI.takeScreenshot(shot + 'TC-09.1_ERROR.png')
	KeywordUtil.markFailed('TC-09.1 berhenti: ' + e.getMessage())
} finally {
	WebUI.closeBrowser()
}