DROP TABLE worldSeed;

CREATE TABLE worldSeed (id INTEGER PRIMARY KEY, seed TEXT, environmentType TEXT);

INSERT INTO worldSeed (seed, environmentType) VALUES ('-3253779088524713661', 'normal');

SELECT * FROM worldSeed;

CREATE TABLE game (id INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, seedGame TEXT, seedNether TEXT);

CREATE TABLE gamePlayer (
  uid TEXT NOT NULL,
  gameId INT NOT NULL REFERENCES game(id),
  deathTick INT NULL,
  place INT NULL
);

