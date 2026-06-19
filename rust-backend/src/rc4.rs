/// RC4 stream cipher — compatible with the Kotlin SpiceConnection implementation.
pub struct RC4 {
    a: usize,
    b: usize,
    sbox: [u8; 256],
}

impl RC4 {
    pub fn new(key: &[u8]) -> Self {
        let mut sbox = [0u8; 256];
        for (i, v) in sbox.iter_mut().enumerate() {
            *v = i as u8;
        }

        let mut j: usize = 0;
        for i in 0..256 {
            j = (j + sbox[i] as usize + key[i % key.len()] as usize) % 256;
            sbox.swap(i, j);
        }

        Self { a: 0, b: 0, sbox }
    }

    /// Encrypt/decrypt in-place (RC4 is symmetric).
    pub fn crypt(&mut self, data: &mut [u8]) {
        for byte in data.iter_mut() {
            self.a = (self.a + 1) % 256;
            self.b = (self.b + self.sbox[self.a] as usize) % 256;
            self.sbox.swap(self.a, self.b);

            let k = self.sbox[(self.sbox[self.a] as usize + self.sbox[self.b] as usize) % 256];
            *byte ^= k;
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_rc4_symmetric() {
        let key = b"testpassword";
        let plaintext = b"Hello, Spice!";
        
        let mut encrypted = plaintext.to_vec();
        let mut rc4_enc = RC4::new(key);
        rc4_enc.crypt(&mut encrypted);

        let mut decrypted = encrypted.clone();
        let mut rc4_dec = RC4::new(key);
        rc4_dec.crypt(&mut decrypted);

        assert_eq!(plaintext, decrypted.as_slice());
    }

    #[test]
    fn test_rc4_known_vector() {
        // Key = "Key", plaintext = "Plaintext"
        let key = b"Key";
        let mut data = b"Plaintext".to_vec();
        let mut rc4 = RC4::new(key);
        rc4.crypt(&mut data);
        // Expected: BB F3 16 E8 D9 40 AF 0A D3
        assert_eq!(data, [0xBB, 0xF3, 0x16, 0xE8, 0xD9, 0x40, 0xAF, 0x0A, 0xD3]);
    }
}
