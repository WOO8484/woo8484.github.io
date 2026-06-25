const SPECIES_DATA = [
  {
    id: 'octopus',
    name: '문어',
    nameEn: 'Common Octopus',
    emoji: '🐙',
    badge: '연중',
    sub: '낙지과 두족류',
    season: [1,1,1,1,1,0,0,1,1,1,1,1],
    peakMonths: '9~11월 (봄도 가능)',
    depth: '바닥~중층',
    habitat: '바위 틈, 모래 바닥',
    size: '500g~3kg (대형 5kg↑)',
    tips: '문어는 지능이 높아 루어에 경계심을 갖습니다. 자연스럽게 바닥을 굴러다니는 움직임을 연출하세요. 냄새에도 반응하므로 생미끼 병용도 효과적입니다.',
    prohibited: '외투장 6cm 미만 방류',
    color: '#FF6B6B',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <ellipse cx="40" cy="32" rx="20" ry="18" fill="#E8654A"/>
      <ellipse cx="40" cy="32" rx="20" ry="18" fill="url(#sp-oct-g)"/>
      <defs><radialGradient id="sp-oct-g" cx="40%" cy="35%"><stop offset="0%" stop-color="#FF8A72"/><stop offset="100%" stop-color="#D44A2E"/></radialGradient></defs>
      <circle cx="33" cy="34" r="2.5" fill="#C03020" opacity="0.5"/>
      <circle cx="40" cy="36" r="2.5" fill="#C03020" opacity="0.5"/>
      <circle cx="47" cy="34" r="2.5" fill="#C03020" opacity="0.5"/>
      <ellipse cx="34" cy="26" rx="6" ry="7" fill="white"/>
      <ellipse cx="46" cy="26" rx="6" ry="7" fill="white"/>
      <ellipse cx="34" cy="27" rx="3.5" ry="4" fill="#1A1A2E"/>
      <ellipse cx="46" cy="27" rx="3.5" ry="4" fill="#1A1A2E"/>
      <circle cx="35.5" cy="25.5" r="1.5" fill="white"/>
      <circle cx="47.5" cy="25.5" r="1.5" fill="white"/>
      <path d="M36 38 Q40 41 44 38" stroke="#C03020" stroke-width="1.5" fill="none" stroke-linecap="round"/>
      <path d="M23 44 C20 50 18 57 21 63 C23 66 25 65 26 62 C27 57 26 51 25 46" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M28 47 C25 54 24 62 27 68 C29 71 31 70 32 67 C33 61 31 54 30 48" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M34 49 C32 57 32 64 34 70 C35 72 37 72 38 70 C39 64 38 57 37 50" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M40 50 C39 58 39 65 40 71 C41 73 43 73 43 71 C44 65 43 58 42 50" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M46 49 C46 57 47 64 49 70 C50 72 52 72 53 70 C53 64 51 57 48 50" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M52 47 C53 54 55 61 57 67 C58 70 60 69 61 67 C61 61 58 54 54 48" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M57 44 C58 51 61 57 63 62 C64 65 65 64 65 62 C65 56 62 50 59 45" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
      <path d="M60 40 C62 47 65 52 66 57 C67 60 67 60 66 58 C65 52 62 46 61 41" fill="#E8654A" stroke="#C03020" stroke-width="0.8"/>
    </svg>`
  },
  {
    id: 'squid-han',
    name: '한치',
    nameEn: 'Japanese Flying Squid',
    emoji: '🦑',
    badge: '여름',
    sub: '꼴뚜기과 두족류',
    season: [0,0,0,0,0,1,1,1,1,0,0,0],
    peakMonths: '7~9월 (야간)',
    depth: '중층~표층',
    habitat: '수면 근처, 빛에 모임',
    size: '20~40cm',
    tips: '야간 집어등 주변이 명당. 에기는 야광 계열이 효과적. 수온 23도 이상에서 활성화됩니다.',
    prohibited: '금어기 없음',
    color: '#7B6BB0',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <defs><linearGradient id="sp-han-g" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#9B8EC4"/><stop offset="100%" stop-color="#6A5AA0"/></linearGradient></defs>
      <ellipse cx="22" cy="44" rx="9" ry="18" fill="#B0A4D8" opacity="0.75" transform="rotate(-15 22 44)"/>
      <ellipse cx="58" cy="44" rx="9" ry="18" fill="#B0A4D8" opacity="0.75" transform="rotate(15 58 44)"/>
      <ellipse cx="40" cy="44" rx="11" ry="24" fill="url(#sp-han-g)"/>
      <polygon points="40,10 26,24 54,24" fill="#8878BC"/>
      <ellipse cx="35" cy="34" rx="4.5" ry="5" fill="white"/>
      <ellipse cx="45" cy="34" rx="4.5" ry="5" fill="white"/>
      <ellipse cx="35" cy="35" rx="2.8" ry="3.2" fill="#1A0A3E"/>
      <ellipse cx="45" cy="35" rx="2.8" ry="3.2" fill="#1A0A3E"/>
      <circle cx="36.2" cy="33.5" r="1.2" fill="white"/>
      <circle cx="46.2" cy="33.5" r="1.2" fill="white"/>
      <line x1="34" y1="68" x2="30" y2="78" stroke="#6A5AA0" stroke-width="2.5" stroke-linecap="round"/>
      <line x1="37" y1="68" x2="34" y2="78" stroke="#6A5AA0" stroke-width="2.2" stroke-linecap="round"/>
      <line x1="40" y1="68" x2="38" y2="79" stroke="#8878BC" stroke-width="2.5" stroke-linecap="round"/>
      <line x1="43" y1="68" x2="44" y2="79" stroke="#8878BC" stroke-width="2.2" stroke-linecap="round"/>
      <line x1="46" y1="68" x2="50" y2="78" stroke="#6A5AA0" stroke-width="2.5" stroke-linecap="round"/>
      <ellipse cx="38" cy="50" rx="2.5" ry="1.5" fill="#5A4A8A" opacity="0.5"/>
      <ellipse cx="43" cy="50" rx="2.5" ry="1.5" fill="#5A4A8A" opacity="0.5"/>
      <ellipse cx="40" cy="56" rx="3" ry="1.5" fill="#5A4A8A" opacity="0.4"/>
    </svg>`
  },
  {
    id: 'webfoot',
    name: '주꾸미',
    nameEn: 'Webfoot Octopus',
    emoji: '🐠',
    badge: '가을~겨울',
    sub: '문어과 두족류',
    season: [0,0,0,0,0,0,0,0,1,1,1,0],
    peakMonths: '9~11월',
    depth: '바닥층',
    habitat: '모래·뻘 바닥, 조개껍데기 틈',
    size: '100~300g',
    tips: '초보자도 쉽게 즐길 수 있는 어종. 에기를 잡으면 놓지 않는 습성이 있어 천천히 들어올리면 됩니다. 금어기(5월 11일~8월 31일)에는 낚시 금지!',
    prohibited: '⛔ 금어기 5/11~8/31',
    color: '#FF9B71',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <defs><radialGradient id="sp-juk-g" cx="40%" cy="35%"><stop offset="0%" stop-color="#FFAF88"/><stop offset="100%" stop-color="#E07040"/></radialGradient></defs>
      <circle cx="40" cy="30" r="18" fill="url(#sp-juk-g)"/>
      <ellipse cx="40" cy="28" rx="10" ry="6" fill="none" stroke="#FFD166" stroke-width="1.5" opacity="0.7"/>
      <ellipse cx="34" cy="25" rx="5" ry="6" fill="white"/>
      <ellipse cx="46" cy="25" rx="5" ry="6" fill="white"/>
      <ellipse cx="34" cy="26" rx="3" ry="3.5" fill="#1A1A1A"/>
      <ellipse cx="46" cy="26" rx="3" ry="3.5" fill="#1A1A1A"/>
      <circle cx="35.2" cy="24.5" r="1.3" fill="white"/>
      <circle cx="47.2" cy="24.5" r="1.3" fill="white"/>
      <path d="M36 35 Q40 38 44 35" stroke="#C06030" stroke-width="1.5" fill="none" stroke-linecap="round"/>
      <path d="M22 43 C20 49 20 55 22 60 C23 62 25 62 26 60 C27 55 26 49 25 44" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M27 46 C25 52 25 58 27 63 C28 65 30 65 31 63 C32 58 30 52 29 47" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M33 48 C31 54 31 60 33 65 C34 66 36 66 37 65 C37 59 36 53 35 49" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M40 48 C39 54 39 60 40 65 C41 67 43 66 43 65 C44 59 43 53 42 49" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M47 48 C47 54 48 60 49 65 C50 66 52 66 53 65 C53 59 51 53 49 49" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M53 46 C54 52 55 58 56 63 C57 65 59 65 59 63 C59 58 57 52 55 47" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M58 43 C59 49 61 55 62 60 C63 62 64 62 65 60 C65 55 62 49 60 44" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
      <path d="M61 39 C63 45 64 51 64 56 C64 58 65 58 65 56 C65 50 63 44 62 40" fill="#FF9B71" stroke="#D06030" stroke-width="0.8"/>
    </svg>`
  },
  {
    id: 'bigfin',
    name: '무늬오징어',
    nameEn: 'Bigfin Reef Squid',
    emoji: '🦑',
    badge: '봄·가을',
    sub: '참꼴뚜기과 두족류',
    season: [0,0,1,1,1,0,0,0,1,1,1,0],
    peakMonths: '3~5월, 9~11월',
    depth: '중·하층 (밤엔 표층)',
    habitat: '해조류 많은 갯바위, 얕은 여',
    size: '200g~1kg',
    tips: '에깅의 핵심 타겟! 조류 흐름과 수온에 민감합니다. 산란기(봄)에는 얕은 해조류 지대를 공략하세요. 폴 중 어택이 많으니 라인 관리를 철저히 합니다.',
    prohibited: '금어기 없음 (산란기 자제 권장)',
    color: '#3BAED4',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <defs><linearGradient id="sp-mun-g" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#5DCDE8"/><stop offset="100%" stop-color="#2A8AAE"/></linearGradient></defs>
      <ellipse cx="18" cy="42" rx="12" ry="20" fill="#80D8F0" opacity="0.8" transform="rotate(-20 18 42)"/>
      <ellipse cx="62" cy="42" rx="12" ry="20" fill="#80D8F0" opacity="0.8" transform="rotate(20 62 42)"/>
      <ellipse cx="40" cy="44" rx="13" ry="26" fill="url(#sp-mun-g)"/>
      <polygon points="40,8 25,26 55,26" fill="#4AAFC8"/>
      <ellipse cx="36" cy="38" rx="3.5" ry="2.5" fill="#1A7AA0" opacity="0.6"/>
      <ellipse cx="44" cy="38" rx="3.5" ry="2.5" fill="#1A7AA0" opacity="0.6"/>
      <ellipse cx="40" cy="44" rx="4" ry="2.5" fill="#1A7AA0" opacity="0.5"/>
      <ellipse cx="36" cy="50" rx="3" ry="2" fill="#1A7AA0" opacity="0.5"/>
      <ellipse cx="44" cy="50" rx="3" ry="2" fill="#1A7AA0" opacity="0.5"/>
      <ellipse cx="40" cy="56" rx="3.5" ry="2" fill="#1A7AA0" opacity="0.4"/>
      <ellipse cx="34" cy="32" rx="5" ry="5.5" fill="white"/>
      <ellipse cx="46" cy="32" rx="5" ry="5.5" fill="white"/>
      <ellipse cx="34" cy="33" rx="3" ry="3.5" fill="#0A2840"/>
      <ellipse cx="46" cy="33" rx="3" ry="3.5" fill="#0A2840"/>
      <circle cx="35.2" cy="31.5" r="1.4" fill="white"/>
      <circle cx="47.2" cy="31.5" r="1.4" fill="white"/>
      <line x1="35" y1="70" x2="31" y2="79" stroke="#2A8AAE" stroke-width="2.2" stroke-linecap="round"/>
      <line x1="38" y1="70" x2="35" y2="79" stroke="#2A8AAE" stroke-width="2" stroke-linecap="round"/>
      <line x1="40" y1="70" x2="40" y2="80" stroke="#4AAFC8" stroke-width="2.5" stroke-linecap="round"/>
      <line x1="42" y1="70" x2="45" y2="79" stroke="#2A8AAE" stroke-width="2" stroke-linecap="round"/>
      <line x1="45" y1="70" x2="49" y2="79" stroke="#2A8AAE" stroke-width="2.2" stroke-linecap="round"/>
    </svg>`
  },
  {
    id: 'cuttlefish',
    name: '갑오징어',
    nameEn: 'Common Cuttlefish',
    emoji: '🦑',
    badge: '가을~겨울',
    sub: '갑오징어과 두족류',
    season: [0,0,0,0,0,0,0,0,1,1,1,1],
    peakMonths: '9~12월',
    depth: '바닥~중층',
    habitat: '모래 바닥, 해조류 지대',
    size: '300g~1.5kg',
    tips: '갑오징어는 먹물이 매우 많습니다. 타월과 앞치마 준비 필수. 에기나 쿠레 채비 모두 사용 가능. 선상 오모리그에도 잘 낚입니다.',
    prohibited: '금어기 없음',
    color: '#6CA030',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <defs><radialGradient id="sp-cut-g" cx="45%" cy="40%"><stop offset="0%" stop-color="#A8CC60"/><stop offset="100%" stop-color="#5A8820"/></radialGradient></defs>
      <ellipse cx="40" cy="44" rx="22" ry="26" fill="#B8DC78" opacity="0.7"/>
      <ellipse cx="40" cy="44" rx="16" ry="22" fill="url(#sp-cut-g)"/>
      <ellipse cx="40" cy="38" rx="13" ry="14" fill="#C8E880" opacity="0.6"/>
      <ellipse cx="40" cy="38" rx="10" ry="11" fill="none" stroke="#7AAA30" stroke-width="1" opacity="0.8"/>
      <ellipse cx="34" cy="36" rx="3.5" ry="2.5" fill="#4A7010" opacity="0.55"/>
      <ellipse cx="46" cy="36" rx="3.5" ry="2.5" fill="#4A7010" opacity="0.55"/>
      <ellipse cx="40" cy="42" rx="4" ry="2.5" fill="#4A7010" opacity="0.5"/>
      <ellipse cx="34" cy="48" rx="3" ry="2" fill="#4A7010" opacity="0.45"/>
      <ellipse cx="46" cy="48" rx="3" ry="2" fill="#4A7010" opacity="0.45"/>
      <ellipse cx="33" cy="30" rx="5.5" ry="6" fill="white"/>
      <ellipse cx="47" cy="30" rx="5.5" ry="6" fill="white"/>
      <ellipse cx="33" cy="31" rx="3.5" ry="4" fill="#0A1A04"/>
      <ellipse cx="47" cy="31" rx="3.5" ry="4" fill="#0A1A04"/>
      <circle cx="34.5" cy="29.5" r="1.5" fill="white"/>
      <circle cx="48.5" cy="29.5" r="1.5" fill="white"/>
      <line x1="36" y1="66" x2="32" y2="76" stroke="#5A8820" stroke-width="2.2" stroke-linecap="round"/>
      <line x1="38.5" y1="66" x2="36" y2="76" stroke="#5A8820" stroke-width="2" stroke-linecap="round"/>
      <line x1="40" y1="66" x2="40" y2="77" stroke="#7AAA30" stroke-width="2.5" stroke-linecap="round"/>
      <line x1="41.5" y1="66" x2="44" y2="76" stroke="#5A8820" stroke-width="2" stroke-linecap="round"/>
      <line x1="44" y1="66" x2="48" y2="76" stroke="#5A8820" stroke-width="2.2" stroke-linecap="round"/>
    </svg>`
  },
  {
    id: 'arrow',
    name: '화살촉오징어',
    nameEn: 'Japanese Flying Squid (juvenile)',
    emoji: '🦑',
    badge: '여름~가을',
    sub: '살오징어과 두족류 (살오징어 새끼)',
    season: [0,0,0,0,0,1,1,1,1,1,1,0],
    peakMonths: '6~11월',
    depth: '표층~40m',
    habitat: '연안 방파제, 항구 주변, 집어등 아래',
    size: '10~20cm (외투장)',
    tips: '아무 기술이 필요 없는 마릿수 낚시! 집어등 아래 채비를 내리면 됩니다. 입질층 찾기가 핵심 — 20m 지점에 면사 마킹 추천. 폴링 중 입질이 많으므로 채비가 가라앉는 동안 집중하세요.',
    prohibited: '금어기 5월 / 몸통 15cm 이하 방류',
    color: '#E07B39',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <defs><linearGradient id="sp-arr-g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#F0A060"/><stop offset="100%" stop-color="#C05820"/></linearGradient></defs>
      <!-- 화살촉오징어: 가늘고 긴 화살 모양 몸통 -->
      <!-- 지느러미 (화살촉 모양) -->
      <polygon points="40,8 48,28 40,22 32,28" fill="#E8904A" opacity="0.85"/>
      <!-- 몸통 (가늘고 길쭉) -->
      <ellipse cx="40" cy="42" rx="9" ry="28" fill="url(#sp-arr-g)"/>
      <!-- 광택 -->
      <ellipse cx="37" cy="32" rx="3" ry="10" fill="white" opacity="0.18"/>
      <!-- 점무늬 (화살촉오징어 특징) -->
      <circle cx="36" cy="35" r="2" fill="#A03A08" opacity="0.4"/>
      <circle cx="44" cy="38" r="1.8" fill="#A03A08" opacity="0.4"/>
      <circle cx="37" cy="45" r="2" fill="#A03A08" opacity="0.35"/>
      <circle cx="43" cy="48" r="1.6" fill="#A03A08" opacity="0.35"/>
      <!-- 눈 -->
      <ellipse cx="35" cy="26" rx="4.5" ry="5" fill="white"/>
      <ellipse cx="45" cy="26" rx="4.5" ry="5" fill="white"/>
      <ellipse cx="35" cy="27" rx="3" ry="3.5" fill="#1A0A00"/>
      <ellipse cx="45" cy="27" rx="3" ry="3.5" fill="#1A0A00"/>
      <circle cx="36" cy="25.5" r="1.2" fill="white"/>
      <circle cx="46" cy="25.5" r="1.2" fill="white"/>
      <!-- 촉수 -->
      <line x1="36" y1="70" x2="32" y2="78" stroke="#C05820" stroke-width="1.8" stroke-linecap="round"/>
      <line x1="38" y1="70" x2="36" y2="79" stroke="#C05820" stroke-width="1.8" stroke-linecap="round"/>
      <line x1="40" y1="70" x2="40" y2="79" stroke="#E07030" stroke-width="2.2" stroke-linecap="round"/>
      <line x1="42" y1="70" x2="44" y2="79" stroke="#C05820" stroke-width="1.8" stroke-linecap="round"/>
      <line x1="44" y1="70" x2="48" y2="78" stroke="#C05820" stroke-width="1.8" stroke-linecap="round"/>
    </svg>`
  },
  {
    id: 'saury',
    name: '살오징어',
    nameEn: 'Japanese Flying Squid',
    emoji: '🦑',
    badge: '봄~가을',
    sub: '살오징어과 두족류 (먼바다 원양)',
    season: [0,0,0,1,1,1,1,1,1,1,0,0],
    peakMonths: '6~9월 (남해 먼바다)',
    depth: '표층~40m (입질층 탐색 중요)',
    habitat: '남해 먼바다 원도권, 동해안',
    size: '20~40cm (외투장)',
    tips: '공격성이 강해 다양한 루어에 잘 반응합니다. 입질층 찾기가 최우선 — 바닥에서 3~5바퀴 릴링 후 폴링 반복. 피크타임은 자정~새벽 3시. 집어등 효과 탁월. 세 자릿수 마릿수 조과도 가능!',
    prohibited: '금어기 4월 1일 ~ 5월 31일',
    color: '#4A7FC0',
    svgPath: `<svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg">
      <defs><linearGradient id="sp-sau-g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#78B0E8"/><stop offset="100%" stop-color="#2A5898"/></linearGradient></defs>
      <polygon points="40,6 50,24 40,20 30,24" fill="#5A90C8" opacity="0.9"/>
      <ellipse cx="40" cy="43" rx="12" ry="30" fill="url(#sp-sau-g)"/>
      <ellipse cx="36" cy="35" rx="3.5" ry="14" fill="white" opacity="0.15"/>
      <ellipse cx="40" cy="50" rx="8" ry="18" fill="#C8E0F8" opacity="0.25"/>
      <circle cx="35" cy="33" r="2.2" fill="#1A3870" opacity="0.35"/>
      <circle cx="45" cy="37" r="2" fill="#1A3870" opacity="0.35"/>
      <circle cx="36" cy="44" r="2.2" fill="#1A3870" opacity="0.3"/>
      <circle cx="44" cy="50" r="2" fill="#1A3870" opacity="0.3"/>
      <ellipse cx="34" cy="24" rx="5.5" ry="6" fill="white"/>
      <ellipse cx="46" cy="24" rx="5.5" ry="6" fill="white"/>
      <ellipse cx="34" cy="25" rx="3.8" ry="4.2" fill="#0A1830"/>
      <ellipse cx="46" cy="25" rx="3.8" ry="4.2" fill="#0A1830"/>
      <circle cx="35.5" cy="23.5" r="1.4" fill="white"/>
      <circle cx="47.5" cy="23.5" r="1.4" fill="white"/>
      <line x1="36" y1="73" x2="31" y2="79" stroke="#2A5898" stroke-width="2" stroke-linecap="round"/>
      <line x1="38" y1="73" x2="35" y2="79" stroke="#2A5898" stroke-width="2" stroke-linecap="round"/>
      <line x1="40" y1="73" x2="40" y2="80" stroke="#3A6EC8" stroke-width="2.5" stroke-linecap="round"/>
      <line x1="42" y1="73" x2="45" y2="79" stroke="#2A5898" stroke-width="2" stroke-linecap="round"/>
      <line x1="44" y1="73" x2="49" y2="79" stroke="#2A5898" stroke-width="2" stroke-linecap="round"/>
    </svg>`
  }
];
