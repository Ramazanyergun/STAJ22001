from sfm import Sfm
from CreateMesh import Mesh


def main():
    dataname = 'Ground'
    ply_path = './res/' + dataname +'.ply'
    result_path = './OUTPUT/'

    # SFM işlemini başlat
    sfm = Sfm("Datasets\\" + dataname)
    sfm()  # Nokta bulutu ve diğer veriler bu adımda oluşturuluyor
    mesh = Mesh(ply_path, result_path, dataname)

    # Ply verisini kullanarak mesh oluştur ve kaydet
    mesh.main(result_path, dataname)


if __name__ == '__main__':
    main()
