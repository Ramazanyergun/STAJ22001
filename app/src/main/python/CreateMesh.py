import open3d as o3d
import numpy as np


class Mesh:
    def __init__(self, ply_path, result_path, dataname):
        self.ply_path = ply_path
        self.result_path = result_path
        self.dataname = dataname

    def read_point_cloud(self):
        pcd = o3d.io.read_point_cloud(self.ply_path)
        return pcd

    def process_point_cloud(self, pcd):
        # Nokta bulutu üzerinde normal tahmini
        if isinstance(pcd, o3d.geometry.PointCloud):
            pcd.estimate_normals(search_param=o3d.geometry.KDTreeSearchParamHybrid(radius=0.1, max_nn=30))
        return pcd

    def create_mesh(self, point_cloud):
        if isinstance(point_cloud, o3d.geometry.PointCloud):
            distances = point_cloud.compute_nearest_neighbor_distance()
            avg_dist = np.mean(distances)
            radius = 6 * avg_dist  # Noktaların yoğunluğuna göre ayarlanabilir
            mesh = o3d.geometry.TriangleMesh.create_from_point_cloud_ball_pivoting(
                point_cloud, o3d.utility.DoubleVector([radius, radius * 2, radius * 4])
            )
            return mesh
        else:
            raise ValueError("The input is not a valid PointCloud object")

    def save_mesh(self, mesh, output_path, dataname):
        # Yüzeyi dosyaya yazma
        o3d.io.write_triangle_mesh(output_path + dataname + ".obj", mesh)
        print("Model Kaydedildi")

    def main(self, output_path, dataname):
        pcd = self.read_point_cloud()
        pcd = self.process_point_cloud(pcd)
        mesh = self.create_mesh(pcd)
        self.save_mesh(mesh, output_path, dataname)
