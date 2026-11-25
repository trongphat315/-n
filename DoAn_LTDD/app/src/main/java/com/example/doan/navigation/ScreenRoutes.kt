package com.example.doan.navigation

sealed class Screen(val route: String) {
    data object XacThuc : Screen("mh_01_xac_thuc")
    data object TrangChu : Screen("mh_02_trang_chu/{userName}") {
        fun createRoute(userName: String) = "mh_02_trang_chu/$userName"
    }

    data object DanhSachChuDe : Screen("mh_03_danh_sach_chu_de")
    data object LamBaiThi : Screen("mh_04_lam_bai_thi/{quizId}/{quizTitle}") {
        fun createRoute(quizId: Int, quizTitle: String) = "mh_04_lam_bai_thi/$quizId/$quizTitle"
    }
    data object KetQua : Screen("mh_05_ket_qua/{attemptId}") {
        fun createRoute(attemptId: String) = "mh_05_ket_qua/$attemptId"
    }

    data object LichSu : Screen("mh_06_lich_su")
    data object ChiTietLichSu : Screen("mh_07_chi_tiet_lich_su/{attemptId}") {
        fun createRoute(attemptId: String) = "mh_07_chi_tiet_lich_su/$attemptId"
    }

    data object SanhThachDau : Screen("mh_08_sanh_thach_dau")
    data object ThiDau1v1 : Screen("mh_09_thi_dau_1v1/{matchId}") {
        fun createRoute(matchId: String) = "mh_09_thi_dau_1v1/$matchId"
    }

    data object BangXepHang : Screen("mh_10_bang_xep_hang")
    data object BanBe : Screen("mh_11_ban_be")
    data object CaNhan : Screen("mh_12_ca_nhan")
}