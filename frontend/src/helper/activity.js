import styles from './styles/activity.module.css';

const colors = ['#288FE4', '#FFE87E', '#DB7477', '#1BAA5E', '#DB7477', '#DB7477', '#288FE4', '#C78750'];

export default function ActivityLetters() {
  const letters = "ACTIVITY".split("");

  return letters.map((char, i) => (
    <li
      key={i}
      className={styles.activityLetter}
      style={{ backgroundColor: colors[i % colors.length] }}
    >
      {char}
    </li>
  ));
}


