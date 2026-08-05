DELETE FROM gears
WHERE name = 'レインポンチョ'
  AND id = (SELECT MAX(id) FROM gears WHERE name = 'レインポンチョ');
