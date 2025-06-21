import styles from './styles/date.module.css';

const colors = ['#288FE4', '#FFE87E', '#DB7477', '#1BAA5E'];

export default function DateLetters() {
  const letters = "DATE".split("");

  return letters.map((char, i) => (
    <li
      key={i}
      className={styles.dateLetter}
      style={{ backgroundColor: colors[i % colors.length] }}
    >
      {char}
    </li>
  ));
}