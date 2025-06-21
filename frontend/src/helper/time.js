import styles from './styles/time.module.css';

const colors = ['#288FE4', '#FFE87E', '#DB7477', '#1BAA5E'];

export default function TimeLetters() {
  const letters = "TIME".split("");

  return letters.map((char, i) => (
    <li
      key={i}
      className={styles.timeLetter}
      style={{ backgroundColor: colors[i % colors.length] }}
    >
      {char}
    </li>
  ));
}