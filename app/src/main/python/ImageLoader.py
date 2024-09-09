import os
import numpy as np

class ImageLoader():
    def __init__(self, img_dir: str):
        k_file = img_dir + '\\K.txt'

        # K.txt dosyasının var olup olmadığını kontrol et
        if not os.path.exists(k_file):
            # Eğer K.txt yoksa, varsayılan K matrisi oluştur
            print(f"{k_file} bulunamadı. Varsayılan K matrisi oluşturuluyor.")
            default_K = np.array([[1000, 0, 320],
                                  [0, 1000, 240],
                                  [0, 0, 1]])

            # Varsayılan K matrisini K.txt dosyasına yaz
            with open(k_file, 'w') as f:
                for row in default_K:
                    f.write(' '.join(map(str, row)) + '\n')

        # K.txt dosyasını yükle
        with open(k_file) as f:
            self.K = np.array(
                list((map(lambda x: list(map(lambda x: float(x), x.strip().split(' '))), f.read().split('\n')))))

        self.image_list = []

        # Resim dosyalarını yükle
        for image in sorted(os.listdir(img_dir)):
            if image[-4:].lower() == '.jpg' or image[-4:].lower() == '.png' or image[-5:].lower() == '.jpeg':
                self.image_list.append(img_dir + '\\' + image)

        self.path = os.getcwd()
