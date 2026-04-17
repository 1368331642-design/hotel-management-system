import pymysql

# 数据库配置
config = {
    'host': 'localhost',
    'user': 'root',
    'password': '123456',
    'database': 'hotel_management',
    'charset': 'utf8mb4'
}

# 读取SQL文件
with open('database.sql', 'r', encoding='utf-8') as f:
    sql_content = f.read()

# 连接数据库并执行
try:
    conn = pymysql.connect(**config)
    cursor = conn.cursor()
    
    # 分割SQL语句
    statements = []
    current_statement = []
    
    for line in sql_content.split('\n'):
        line = line.strip()
        if not line or line.startswith('--'):
            continue
        current_statement.append(line)
        if line.endswith(';'):
            statements.append(' '.join(current_statement))
            current_statement = []
    
    # 执行每个语句
    for stmt in statements:
        try:
            cursor.execute(stmt)
            print(f"执行成功: {stmt[:50]}...")
        except Exception as e:
            print(f"执行失败: {stmt[:50]}...")
            print(f"错误: {e}")
    
    conn.commit()
    print("\n数据初始化完成！")
    
except Exception as e:
    print(f"错误: {e}")
finally:
    if 'conn' in locals():
        conn.close()
