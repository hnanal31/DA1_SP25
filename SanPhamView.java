/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import java.awt.Frame;
import entity.*;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.table.DefaultTableModel;
import repository.*;
import javax.swing.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import response.SanPhamChiTietResponse;
import util.DBConnect;

/**
 *
 * @author THUY DUONG
 */
public class SanPhamView extends javax.swing.JPanel {

    private SanPhamRepo rp = new SanPhamRepo();
    private DefaultTableModel mol = new DefaultTableModel();

    ////spct
    private SanPhamChiTietRepo rpspct = new SanPhamChiTietRepo();
    private DefaultTableModel molspct = new DefaultTableModel();
    ////San Pham chi tiet////

    ////thuoc tinh
    private ThuocTinhRepo thuocTinhRepo = new ThuocTinhRepo();
    private DefaultTableModel model;
    private String selectedTable;
    private String matt;

    public SanPhamView() {
        initComponents();
        add(new JLabel("Sản Phẩm"));
        this.showDataTableSP(rp.getAll());

        tblSP.setRowHeight(25);
        tblSP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSPMouseClicked(evt);
            }
        });
        txtIDSP.setVisible(false);

        model = new DefaultTableModel(new String[]{"STT", "Mã Thuộc Tính", "Tên Thuộc Tính",}, 0);
        tblThuocTinh.setModel(model);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rdoMauSac);
        buttonGroup.add(rdoChatLieu);
        buttonGroup.add(rdoKichThuoc);

        rdoMauSac.addActionListener(e -> {
            if (rdoMauSac.isSelected()) {
                selectedTable = "MauSac";
                matt = "ma_mau";
                hienThiThuocTinhTrenBang(selectedTable);
            }
        });
        rdoChatLieu.addActionListener(e -> {
            if (rdoChatLieu.isSelected()) {
                selectedTable = "ChatLieu";
                matt = "ma_chat_lieu";
                hienThiThuocTinhTrenBang(selectedTable);
            }
        });
        rdoKichThuoc.addActionListener(e -> {
            if (rdoKichThuoc.isSelected()) {
                selectedTable = "Size";
                matt = "ma_size";
                hienThiThuocTinhTrenBang(selectedTable);
            }
        });
        // Hiển thị bảng thuộc tính màu sắc mặc định khi mở form
        rdoMauSac.setSelected(true);
        hienThiThuocTinhTrenBang("MauSac");
        selectedTable = "MauSac";
        matt = "ma_mau";
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("SP");
        SanPhamView jbh = new SanPhamView();
        jFrame.add(jbh);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void showDataTableSP(ArrayList<SanPham> list) {
        mol = (DefaultTableModel) tblSP.getModel();
        mol.setRowCount(0);
        AtomicInteger index = new AtomicInteger(1);
        list.forEach(s -> mol.addRow(new Object[]{
            index.getAndIncrement(), s.getMaSanPham(), s.getTenSanPham(),
            s.getTrangThai() == 1 ? "Còn hàng" : "Hết hàng"

        }));

    }

    private void detailSanPham(int index) {
        SanPham sp = rp.getAll().get(index);
        txtMaSP.setText(sp.getMaSanPham());
        txtTenSP.setText(sp.getTenSanPham());
        rdoConHang.setSelected(sp.getTrangThai() == 1);
        rdoHetHang.setSelected(sp.getTrangThai() == 0);
        txtIDSP.setText(sp.getIdSanPham() + "");
    }

    private SanPham getFormData() {
        try {
            SanPham sanPham = new SanPham();
            sanPham.setMaSanPham(generateCode()); // Tạo mã sản phẩm tự động
            sanPham.setTenSanPham(txtTenSP.getText()); // Lấy tên sản phẩm từ TextField
            sanPham.setTrangThai(rdoConHang.isSelected() ? 1 : 0); // Trạng thái: 1 = Còn hàng, 0 = Hết hàng
            return sanPham;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String generateCode() {
        String prefix = "SP";
        int nextNumber = 1;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT TOP 1 ma_san_pham FROM SanPham WHERE ma_san_pham LIKE 'SP%' "
                + "ORDER BY CAST(SUBSTRING(ma_san_pham, 3, LEN(ma_san_pham)) AS INT) DESC"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextNumber = Integer.parseInt(rs.getString(1).substring(2)) + 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return prefix + String.format("%02d", nextNumber);
    }

    private void clearInputFields() {
        txtMaSP.setText("");
        txtTenSP.setText("");

    }

    private void themDuLieuVaoCombobox(javax.swing.JComboBox<String> comboBox, String tenThuocTinh) {
        Set<String> giaTriDuyNhat = new HashSet<>();
        giaTriDuyNhat.add("Tất cả");
        rpspct.getAllSPCT().forEach(spct -> {
            try {
                String giaTri = String.valueOf(spct.getClass().getMethod("get" + Character.toUpperCase(tenThuocTinh.charAt(0)) + tenThuocTinh.substring(1)).invoke(spct));
                giaTriDuyNhat.add(giaTri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        List<String> sortedList = new ArrayList<>(giaTriDuyNhat);
        int tatCaIndex = sortedList.indexOf("Tất cả");
        if (tatCaIndex != -1) { // Kiểm tra xem "Tất cả" có tồn tại trong danh sách không
            Collections.swap(sortedList, 0, tatCaIndex);
        }
        comboBox.setModel(new javax.swing.DefaultComboBoxModel<>(sortedList.toArray(new String[0])));

    }

    private void detailSanPhamCT(int index) {
        SanPhamChiTietResponse spct = rpspct.getAllSPCT().get(index);

    }

    private void hienThiThuocTinhTrenBang(String tenBang) {
        model.setRowCount(0);
        ArrayList<Object[]> listThuocTinh = thuocTinhRepo.getAllThuocTinh(tenBang);
        for (int i = 0; i < listThuocTinh.size(); i++) {
            Object[] thuocTinh = listThuocTinh.get(i);
            model.addRow(new Object[]{i + 1, thuocTinh[0], thuocTinh[1]});
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtMaSP = new javax.swing.JTextField();
        txtTenSP = new javax.swing.JTextField();
        rdoConHang = new javax.swing.JRadioButton();
        rdoHetHang = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnLamMoi0 = new javax.swing.JButton();
        txtIDSP = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSP = new javax.swing.JTable();
        btnSearch = new javax.swing.JButton();
        txtTimKiem = new javax.swing.JTextField();
        btnLamMoi1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        rdoChatLieu = new javax.swing.JRadioButton();
        rdoMauSac = new javax.swing.JRadioButton();
        rdoKichThuoc = new javax.swing.JRadioButton();
        txtMaThuocTinh = new javax.swing.JTextField();
        txtTenThuocTinh = new javax.swing.JTextField();
        btnThem2 = new javax.swing.JButton();
        btnSua2 = new javax.swing.JButton();
        btnLamMoi2 = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblThuocTinh = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thông Tin Sản Phẩm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Mã Sản Phẩm");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Tên Sản Phẩm");

        txtMaSP.setEditable(false);

        buttonGroup1.add(rdoConHang);
        rdoConHang.setText("Còn Hàng");

        buttonGroup1.add(rdoHetHang);
        rdoHetHang.setText("Hết Hàng");
        rdoHetHang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoHetHangActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Trạng Thái");

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnThem.setBackground(new java.awt.Color(0, 204, 51));
        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        btnSua.setBackground(new java.awt.Color(255, 204, 0));
        btnSua.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSua.setText("Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });

        btnLamMoi0.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi0.setText("Làm Mới");
        btnLamMoi0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoi0ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLamMoi0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSua, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnThem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btnThem)
                .addGap(12, 12, 12)
                .addComponent(btnSua)
                .addGap(18, 18, 18)
                .addComponent(btnLamMoi0)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        txtIDSP.setFont(new java.awt.Font("Segoe UI", 0, 8)); // NOI18N
        txtIDSP.setText("Không xóa cái này");
        txtIDSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIDSPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(41, 41, 41)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtTenSP, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addComponent(rdoConHang)
                                    .addGap(18, 18, 18)
                                    .addComponent(rdoHetHang)))))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 300, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(227, 227, 227))
                    .addComponent(txtIDSP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(rdoConHang)
                    .addComponent(rdoHetHang)
                    .addComponent(txtIDSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(49, 49, 49)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTenSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(76, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Danh sách sản phẩm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        jPanel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tblSP.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "STT", "Mã Sản Phẩm", "Tên Sản Phẩm", "Trạng Thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSPMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblSP);

        btnSearch.setBackground(new java.awt.Color(204, 255, 255));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSearch.setText("Tìm Kiếm");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        txtTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTimKiemActionPerformed(evt);
            }
        });

        btnLamMoi1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLamMoi1.setText("Làm Mới");
        btnLamMoi1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoi1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(245, 245, 245)
                .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(btnSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLamMoi1)
                .addGap(31, 31, 31))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnLamMoi1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Sản Phẩm", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel6.setText("Thuộc Tính");

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        rdoChatLieu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoChatLieu.setText("Chất Liệu");

        rdoMauSac.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoMauSac.setText("Màu Sắc");

        rdoKichThuoc.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoKichThuoc.setText("Kích Thước");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoKichThuoc)
                    .addComponent(rdoChatLieu)
                    .addComponent(rdoMauSac, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoMauSac)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(rdoChatLieu)
                .addGap(18, 18, 18)
                .addComponent(rdoKichThuoc)
                .addGap(15, 15, 15))
        );

        txtMaThuocTinh.setEditable(false);
        txtMaThuocTinh.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mã Thuộc Tính", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        txtMaThuocTinh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaThuocTinhActionPerformed(evt);
            }
        });

        txtTenThuocTinh.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tên Thuộc Tính", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        btnThem2.setBackground(new java.awt.Color(0, 204, 102));
        btnThem2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThem2.setText("Thêm");
        btnThem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThem2ActionPerformed(evt);
            }
        });

        btnSua2.setBackground(new java.awt.Color(255, 204, 0));
        btnSua2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSua2.setText("Sửa");
        btnSua2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSua2ActionPerformed(evt);
            }
        });

        btnLamMoi2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi2.setText("Làm Mới");
        btnLamMoi2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoi2ActionPerformed(evt);
            }
        });

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Danh Sách Thuộc Tính", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        tblThuocTinh.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "STT", "Mã Thuộc Tính", "Tên Thuộc Tính"
            }
        ));
        tblThuocTinh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblThuocTinhMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblThuocTinh);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap(70, Short.MAX_VALUE)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 898, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(498, 498, 498)
                .addComponent(jLabel6)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jSeparator2)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(62, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtMaThuocTinh, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTenThuocTinh, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnThem2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(217, 217, 217))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(btnSua2)
                        .addGap(104, 104, 104)
                        .addComponent(btnLamMoi2)
                        .addGap(373, 373, 373))))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(txtMaThuocTinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(txtTenThuocTinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnThem2)
                            .addComponent(btnSua2)
                            .addComponent(btnLamMoi2))
                        .addGap(26, 26, 26)))
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Thuộc Tính", jPanel3);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setText("QUẢN LÝ SẢN PHẨM");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(476, 476, 476)
                .addComponent(jLabel5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 652, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1181, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 710, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rdoHetHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoHetHangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdoHetHangActionPerformed

    private void setView(JPanel root, JPanel node) {
        root.removeAll();
        root.setLayout(new BorderLayout());
        root.add(node);
        root.validate();
        root.repaint();

    }

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
//        SanPham newSanPham = getFormData();
//        if (newSanPham != null) {
//            if (checkFormCreateProduct()) {
//                if (rp.add(newSanPham)) {
//                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
//                    //                clearInputFields();
//                    showDataTableSP(rp.getAll());
//                } else {
//                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        }
        SanPham newSanPham = getFormData();
        // Kiểm tra dữ liệu đầu vào
        if (newSanPham != null && checkFormCreateProduct()) {

            // Kiểm tra sản phẩm trùng lặp
            if (rp.isSanPhamTrung(newSanPham)) {
                JOptionPane.showMessageDialog(this, "Mã sản phẩm hoặc tên sản phẩm đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Thêm sản phẩm nếu không bị trùng
            if (rp.add(newSanPham)) {
                JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
                clearInputFields(); // Xóa form nhập liệu
                showDataTableSP(rp.getAll()); // Cập nhật bảng sản phẩm
            } else {
                JOptionPane.showMessageDialog(this, "Thêm sản phẩm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        int index = tblSP.getSelectedRow();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để sửa.");
            return;
        }
        SanPham sanPham = rp.getAll().get(index);
        SanPham newSanPham = getFormData();
        newSanPham.setIdSanPham(sanPham.getIdSanPham());
        if (!txtTenSP.getText().isBlank()) {
            if (rp.update(newSanPham)) {
                showDataTableSP(rp.getAll());
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);

        }
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnLamMoi0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoi0ActionPerformed
        // TODO add your handling code here:
        txtTenSP.setText("");
        txtMaSP.setText("");
        buttonGroup1.clearSelection();
    }//GEN-LAST:event_btnLamMoi0ActionPerformed

    private boolean isDialogOpen = false;

    private void tblSPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSPMouseClicked

        int index = tblSP.getSelectedRow();
        this.detailSanPham(index);
        Integer idSP = Integer.valueOf(txtIDSP.getText());
        if (evt.getClickCount() == 2 && !isDialogOpen) {
            isDialogOpen = true; // Đánh dấu dialog đang mở
            String tenSP = tblSP.getValueAt(tblSP.getSelectedRow(), 2).toString();
            SanPhamChiTietDialog dialogSPCT = new SanPhamChiTietDialog((Frame) SwingUtilities.getWindowAncestor(this), true, idSP, tenSP);
            dialogSPCT.setVisible(true);
            dialogSPCT.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    isDialogOpen = false; // Đặt lại biến cờ khi dialog đóng
                    showDataTableSP(rp.getAll());
                }
            });
        }
    }//GEN-LAST:event_tblSPMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        String keyword = txtTimKiem.getText();
        ArrayList<SanPham> ketQuaTimKiem = rp.search(keyword);
        if (ketQuaTimKiem != null) {
            showDataTableSP(ketQuaTimKiem);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm phù hợp.");
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTimKiemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTimKiemActionPerformed

    private void txtMaThuocTinhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaThuocTinhActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaThuocTinhActionPerformed

    private void btnThem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThem2ActionPerformed
        // TODO add your haString maThuocTinh = txtMaThuocTinh.getText();
//        String maThuocTinh = txtMaThuocTinh.getText();
//        String tenThuocTinh = txtTenThuocTinh.getText();
//
//        if (tenThuocTinh.isBlank()) {
//            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên thuộc tính.");
//            return;
//        }
//
//        if (tenThuocTinh.length() > 20) {
//            JOptionPane.showMessageDialog(this, "Tên thuộc tính không được quá 20 ký tự.");
//            return;
//        }
//        try {
//
//            JOptionPane.showMessageDialog(this, "Thêm " + selectedTable + " thành công!");
//            hienThiThuocTinhTrenBang(selectedTable);
//            txtMaThuocTinh.setText("");
//            txtTenThuocTinh.setText("");
//
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this, "Lỗi khi thêm " + selectedTable + ": " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace();
//        }
        String tenThuocTinh = txtTenThuocTinh.getText();
        // Kiểm tra dữ liệu đầu vào
        if (tenThuocTinh.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên thuộc tính.");
            return;
        }
        if (tenThuocTinh.length() > 20) {
            JOptionPane.showMessageDialog(this, "Tên thuộc tính không được quá 20 ký tự.");
            return;
        }
        // Xác định bảng và cột cần thao tác
        String tableName = "";
        String columnId = "";
        String columnName = "";
        String prefix = "";
        if (rdoMauSac.isSelected()) {
            tableName = "MauSac";
            columnId = "ma_mau";
            columnName = "ten_mau";
            prefix = "MS";
        } else if (rdoChatLieu.isSelected()) {
            tableName = "ChatLieu";
            columnId = "ma_chat_lieu";
            columnName = "ten_chat_lieu";
            prefix = "CL";
        } else if (rdoKichThuoc.isSelected()) {
            tableName = "Size";
            columnId = "ma_size";
            columnName = "ten_size";
            prefix = "KT";
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại thuộc tính.");
            return;
        }
        // KIỂM TRA TRÙNG TÊN TRƯỚC KHI THÊM
        try (Connection conn = DBConnect.getConnection(); PreparedStatement checkPs = conn.prepareStatement(
                "SELECT COUNT(*) AS count FROM " + tableName + " WHERE " + columnName + " = ?")) {
            checkPs.setString(1, tenThuocTinh);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this, "Tên thuộc tính đã tồn tại trong bảng " + tableName + "!");
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra trùng tên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        // Nếu không bị trùng, thực hiện thêm mới
        String maThuocTinh = generateMaThuocTinh(tableName, columnId, prefix);
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + tableName + " (" + columnId + ", " + columnName + ") VALUES (?, ?)")) {
            ps.setString(1, maThuocTinh);
            ps.setString(2, tenThuocTinh);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm " + tableName + " thành công!");
            hienThiThuocTinhTrenBang(tableName);
            // Xóa dữ liệu input sau khi thêm thành công
            txtMaThuocTinh.setText("");
            txtTenThuocTinh.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm " + tableName + ": " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnThem2ActionPerformed

    private String generateMaThuocTinh(String tableName, String columnId, String prefix) {
        int nextNumber = 1;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT TOP 1 " + columnId + " FROM " + tableName
                + " ORDER BY CAST(SUBSTRING(" + columnId + ", 3, LEN(" + columnId + ")) AS INT) DESC"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextNumber = Integer.parseInt(rs.getString(1).substring(2)) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefix + String.format("%02d", nextNumber);
    }
    private void btnSua2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSua2ActionPerformed
        // TODO add your handling code here:
        if (tblThuocTinh.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Chọn thuộc tính cần sửa");
        } else {
            String maThuocTinh = txtMaThuocTinh.getText();
            String tenThuocTinh = txtTenThuocTinh.getText();

            if (maThuocTinh.isBlank() || tenThuocTinh.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mã và tên thuộc tính.");
                return;
            }

            try {
                thuocTinhRepo.updateThuocTinh(selectedTable, maThuocTinh, tenThuocTinh);
                hienThiThuocTinhTrenBang(selectedTable);
                JOptionPane.showMessageDialog(this, "Sửa thành công");

            } catch (Exception ex) {
                Logger.getLogger(SanPhamView.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_btnSua2ActionPerformed

    private void btnLamMoi2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoi2ActionPerformed
        txtMaThuocTinh.setText("");
        txtTenThuocTinh.setText("");
    }//GEN-LAST:event_btnLamMoi2ActionPerformed

    private void tblThuocTinhMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblThuocTinhMouseClicked
        // TODO add your handling code here:
        txtMaThuocTinh.setText(tblThuocTinh.getValueAt(tblThuocTinh.getSelectedRow(), 1).toString());
        txtTenThuocTinh.setText(tblThuocTinh.getValueAt(tblThuocTinh.getSelectedRow(), 2).toString());
    }//GEN-LAST:event_tblThuocTinhMouseClicked

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
        // TODO add your handling code here:
        //        Node = new JPanelSanPhamCT();
        //        setView(JpanelRoot);
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void btnLamMoi1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoi1ActionPerformed
        // TODO add your handling code here:
        txtTimKiem.setText("");
        this.showDataTableSP(rp.getAll());
    }//GEN-LAST:event_btnLamMoi1ActionPerformed

    private void txtIDSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIDSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIDSPActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLamMoi0;
    private javax.swing.JButton btnLamMoi1;
    private javax.swing.JButton btnLamMoi2;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnSua2;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnThem2;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton rdoChatLieu;
    private javax.swing.JRadioButton rdoConHang;
    private javax.swing.JRadioButton rdoHetHang;
    private javax.swing.JRadioButton rdoKichThuoc;
    private javax.swing.JRadioButton rdoMauSac;
    private javax.swing.JTable tblSP;
    private javax.swing.JTable tblThuocTinh;
    private javax.swing.JTextField txtIDSP;
    private javax.swing.JTextField txtMaSP;
    private javax.swing.JTextField txtMaThuocTinh;
    private javax.swing.JTextField txtTenSP;
    private javax.swing.JTextField txtTenThuocTinh;
    private javax.swing.JTextField txtTimKiem;
    // End of variables declaration//GEN-END:variables
private boolean isValidCouponCode(String str) {
        // Biểu thức chính quy cho phép các ký tự chữ và số
        String regex = "^[$,^,&,*,<,>,|,!,;,:,  ,#,'',+,=,{}]+$";
        return str.matches(regex);
    }

    private boolean checkFormCreateProduct() {

        if (txtTenSP.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!");
            return false;
        }
        if (txtTenSP.getText().length() > 20) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được quá 20 ký tự!");
            return false;
        }
        if (!rdoConHang.isSelected() && !rdoHetHang.isSelected()) {
            JOptionPane.showMessageDialog(this, "Chọn trạng thái cho sản phẩm!");
            return false;
        }
        if (isValidCouponCode(txtMaSP.getText())) {
            JOptionPane.showMessageDialog(this, "Tên mã sản phẩm chỉ được chứa chữ và số.");
            return false;
        }
        if (isValidCouponCode(txtTenSP.getText())) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm chỉ được chứa chữ và số.");
            return false;
        }

        return true;
    }
}
