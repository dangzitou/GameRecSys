CREATE DATABASE IF NOT EXISTS gamerecsys;

USE gamerecsys;

-- 游戏数据表（来自Steam）
CREATE TABLE IF NOT EXISTS game_data (
    AppID INT PRIMARY KEY,
    Name VARCHAR(255),
    Release_date VARCHAR(50),
    Estimated_owners VARCHAR(100),
    Peak_CCU INT,
    Required_age INT,
    Price FLOAT,
    Discount INT,
    DLC_count INT,
    About_the_game TEXT,
    Supported_languages TEXT,
    Full_audio_languages TEXT,
    Reviews TEXT,
    Header_image VARCHAR(1024),
    Website VARCHAR(1024),
    Support_url VARCHAR(1024),
    Support_email VARCHAR(255),
    Windows BOOLEAN,
    Mac BOOLEAN,
    Linux BOOLEAN,
    Metacritic_score INT,
    Metacritic_url VARCHAR(1024),
    User_score INT,
    Positive INT,
    Negative INT,
    Score_rank VARCHAR(50),
    Achievements INT,
    Recommendations INT,
    Notes TEXT,
    Average_playtime_forever INT,
    Average_playtime_two_weeks INT,
    Median_playtime_forever INT,
    Median_playtime_two_weeks INT,
    Developers VARCHAR(255),
    Publishers VARCHAR(255),
    Categories TEXT,
    Genres TEXT,
    Tags TEXT,
    Screenshots TEXT,
    Movies TEXT
);

-- 用户账户表
CREATE TABLE IF NOT EXISTS user_account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户表';

-- 用户游戏评分表
CREATE TABLE IF NOT EXISTS user_game_rating (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    game_id INT NOT NULL COMMENT '游戏ID (对应 AppID)',
    rating DECIMAL(2,1) NOT NULL COMMENT '评分 1.0-5.0',
    timestamp BIGINT NOT NULL COMMENT '评分时间戳',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_game (user_id, game_id),
    INDEX idx_user_id (user_id),
    INDEX idx_game_id (game_id),
    FOREIGN KEY (user_id) REFERENCES user_account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;