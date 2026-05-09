import re

file_path = 'app/src/main/java/com/example/food_order_app/controller/HomeActivity.java'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "private void saveCache(String key, Object data) {" in line and not skip:
        # keep this one, but skip next occurrence
        skip = True
        new_lines.append(line)
    elif "private void saveCache(String key, Object data) {" in line and skip:
        # omit 8 lines
        pass
    else:
        new_lines.append(line)

# Let's do it safely
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# remove duplicate block
dupe = r'''    private void saveCache\(String key, Object data\) \{
        new Thread\(\(\) -> \{
            AppDatabase.getInstance\(this\)\.offlineCacheDao\(\)\.insertCache\(
                new OfflineCache\(key, new Gson\(\)\.toJson\(data\), System\.currentTimeMillis\(\)\)
            \);
        \}\)\.start\(\);
    \}'''

# replace the block twice with a single block
text = re.sub(dupe + r'\s*' + dupe, dupe.replace('\\', ''), text, flags=re.MULTILINE)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)

