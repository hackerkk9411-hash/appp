import re

INPUT  = 'ajj.m3u'
OUTPUT = 'output.m3u'

NEW_UA          = 'plaYtv/7.1.3 (Linux;Android 13) ygx/69.1 ExoPlayerLib/824.0'
NEW_LICENSE_KEY = 'https://tv.media.jio.com/catchupproxy?provider=reliance'
NEW_EXP         = '9999999999'      # expire bahut door
DEFAULT_ST      = '1791698407'

with open(INPUT, 'r', encoding='utf-8') as f:
    content = f.read()

# Har #EXTINF se naya block start
blocks = re.split(r'\n(?=#EXTINF)', content)

results = []

for block in blocks:
    lines = [l for l in block.strip().split('\n') if l.strip()]
    if not lines or not lines[0].startswith('#EXTINF'):
        continue

    extinf = lines[0]

    def attr(name, default=''):
        m = re.search(rf'{name}="([^"]*)"', extinf)
        return m.group(1) if m else default

    logo     = attr('tvg-logo')
    group    = attr('group-title')
    tvg_id   = attr('tvg-id')
    tvg_name = attr('tvg-name')

    # Channel name = last comma ke baad
    name = extinf.rsplit(',', 1)[-1].strip()

    # URL dhoondo
    url = None
    for l in lines:
        if l.startswith('http'):
            url = l.strip()
            break
    if not url:
        continue

    # Original st nikaalo (warna default)
    st_match = re.search(r'st=(\d+)', url)
    st = st_match.group(1) if st_match else DEFAULT_ST

    # URL ke query params hatao, naya __hdnea__ lagao (hmac blank, exp fix)
    base_url = url.split('?')[0]
    new_url  = f"{base_url}?__hdnea__=st={st}~exp={NEW_EXP}~acl=/*~hmac="

    # tvg-id preserve karo (agar hai), warna tvg-name use karo
    if tvg_id:
        id_part = f' tvg-id="{tvg_id}"'
    elif tvg_name:
        id_part = f' tvg-id="{tvg_name}"'
    else:
        id_part = ''

    new_block = (
        f'#EXTINF:-1{id_part} group-title="{group}" tvg-logo="{logo}",{name}\n'
        f'#KODIPROP:inputstream.adaptive.license_type=widevine\n'
        f'#KODIPROP:inputstream.adaptive.license_key={NEW_LICENSE_KEY}\n'
        f'#EXTVLCOPT:http-user-agent={NEW_UA}\n'
        f'{new_url}'
    )
    results.append(new_block)

with open(OUTPUT, 'w', encoding='utf-8') as f:
    f.write('\n\n'.join(results) + '\n')

print(f'✅ Converted {len(results)} channels -> {OUTPUT}')
