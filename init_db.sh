#!/bin/bash

# ==============================================================================
#  Script khởi tạo dữ liệu ban đầu cho ứng dụng WebMusic
#  - Tạo 3 vai trò: ADMIN, CREATOR, USER
#  - Tạo 1 tài khoản Admin: admin@music.com / admin123
#  - Tạo 1 tài khoản Creator: creator@music.com / creator123
# ==============================================================================

# --- CẤU HÌNH KẾT NỐI DATABASE ---
# Thay đổi các giá trị này nếu cần, hoặc đặt chúng làm biến môi trường
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-your_postgres_password} # <<< THAY MẬT KHẨU CỦA BẠN VÀO ĐÂY
DB_NAME=${DB_NAME:-webmusic}

# Export biến PGPASSWORD để psql tự động sử dụng, tránh bị hỏi mật khẩu
export PGPASSWORD=$DB_PASSWORD

echo "Đang kết nối tới database '$DB_NAME' trên '$DB_HOST:$DB_PORT'..."

# Sử dụng "here document" (<<EOF) để truyền nhiều lệnh SQL vào psql
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<EOF

-- Chèn 3 vai trò cơ bản vào bảng 'roles'
-- Gán ID cụ thể để dễ dàng tham chiếu
-- ON CONFLICT DO NOTHING: Nếu đã tồn tại thì bỏ qua, không gây lỗi
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN') ON CONFLICT (id) DO NOTHING;
INSERT INTO roles (id, name) VALUES (2, 'ROLE_CREATOR') ON CONFLICT (id) DO NOTHING;
INSERT INTO roles (id, name) VALUES (3, 'ROLE_USER') ON CONFLICT (id) DO NOTHING;

-- Chèn tài khoản Admin vào bảng 'users'
-- Mật khẩu là 'admin123' đã được mã hóa bằng BCrypt
INSERT INTO users (id, display_name, email, password, status, provider, created_at, updated_at) VALUES
(1, 'Admin', 'admin@music.com', '\$2a\$10\$5x7s.A8x9fV2/VbE2e.mXe5d8w.nZ9z9.uC3Y0fG5r3r3d.w3t.e.', 'ACTIVE', 'LOCAL', NOW(), NOW()) ON CONFLICT (id) DO NOTHING;

-- Chèn tài khoản Creator vào bảng 'users'
-- Mật khẩu là 'creator123' đã được mã hóa bằng BCrypt
INSERT INTO users (id, display_name, email, password, status, provider, created_at, updated_at) VALUES
(2, 'Creator Name', 'creator@music.com', '\$2a\$10\$wE9l1W9o.tY7v.pY8e.qFe7h1yO.oZ2a.bC4Y1gH6s4s4e.x4t.f.', 'ACTIVE', 'LOCAL', NOW(), NOW()) ON CONFLICT (id) DO NOTHING;

-- Thiết lập vai trò cho các tài khoản trong bảng 'user_roles'
-- Tài khoản Admin (user_id=1) sẽ có cả 3 vai trò
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1) ON CONFLICT (user_id, role_id) DO NOTHING; -- ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2) ON CONFLICT (user_id, role_id) DO NOTHING; -- CREATOR
INSERT INTO user_roles (user_id, role_id) VALUES (1, 3) ON CONFLICT (user_id, role_id) DO NOTHING; -- USER

-- Tài khoản Creator (user_id=2) sẽ có vai trò CREATOR và USER
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2) ON CONFLICT (user_id, role_id) DO NOTHING; -- CREATOR
INSERT INTO user_roles (user_id, role_id) VALUES (2, 3) ON CONFLICT (user_id, role_id) DO NOTHING; -- USER

EOF

# Kiểm tra kết quả của lệnh psql
if [ $? -eq 0 ]; then
  echo -e "\033[0;32mThành công! Dữ liệu ban đầu đã được chèn vào database.\033[0m"
  echo "Tài khoản Admin: admin@music.com / admin123"
  echo "Tài khoản Creator: creator@music.com / creator123"
else
  echo -e "\033[0;31mThất bại! Đã có lỗi xảy ra khi chèn dữ liệu.\033[0m"
  echo "Vui lòng kiểm tra lại cấu hình database và các thông báo lỗi ở trên."
fi

# Xóa biến PGPASSWORD khỏi môi trường để tăng cường bảo mật
unset PGPASSWORD