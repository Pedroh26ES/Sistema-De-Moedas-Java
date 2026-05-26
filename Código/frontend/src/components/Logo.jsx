import { useId } from "react";
import { APP_NAME } from "../config/app";

function LogoMark({ className = "", title = "Logo Valoriza A\u00ea" }) {
  const rawId = useId().replace(/:/g, "");
  const capId = `valoriza-logo-cap-${rawId}`;
  const coinId = `valoriza-logo-coin-${rawId}`;

  return (
    <svg
      className={`logo-mark ${className}`.trim()}
      viewBox="0 0 128 128"
      role="img"
      aria-label={title}
      focusable="false"
    >
      <defs>
        <linearGradient id={capId} x1="31" x2="103" y1="25" y2="91" gradientUnits="userSpaceOnUse">
          <stop stopColor="#063a5f" />
          <stop offset="1" stopColor="#021f36" />
        </linearGradient>
        <linearGradient id={coinId} x1="45" x2="87" y1="52" y2="108" gradientUnits="userSpaceOnUse">
          <stop stopColor="#ffd23f" />
          <stop offset="1" stopColor="#f4a900" />
        </linearGradient>
      </defs>
      <path
        d="M18 45.4 60.1 27.2a10 10 0 0 1 7.8 0L110 45.4c4.6 2 4.6 8.4 0 10.4L82.5 67.7C77.4 62.8 70.8 60.2 64 60.2s-13.4 2.6-18.5 7.5L18 55.8c-4.6-2-4.6-8.4 0-10.4Z"
        fill={`url(#${capId})`}
      />
      <path
        d="M44.2 67.3c5.5-5.1 12.4-7.7 19.8-7.7s14.3 2.6 19.8 7.7v17.5c-5.6-5.6-12.4-8.4-19.8-8.4s-14.2 2.8-19.8 8.4V67.3Z"
        fill={`url(#${capId})`}
      />
      <path d="M101.7 53.2v33.2" stroke={`url(#${capId})`} strokeLinecap="round" strokeWidth="7" />
      <circle cx="101.7" cy="89.8" r="6.3" fill={`url(#${capId})`} />
      <path
        d="M94.7 96.5 101.7 88.8l7 7.7 2.3 17.3H92.4l2.3-17.3Z"
        fill={`url(#${capId})`}
      />
      <circle cx="64" cy="82.5" r="32.5" fill={`url(#${coinId})`} />
      <circle cx="64" cy="82.5" r="26.6" fill="none" stroke="#ffffff" strokeWidth="6.2" />
    </svg>
  );
}

function BrandLogo({ className = "", onClick, as: Element = "button" }) {
  return (
    <Element className={`brand ${className}`.trim()} onClick={onClick} type={Element === "button" ? "button" : undefined}>
      <LogoMark />
      <span className="brand-text">{APP_NAME}</span>
    </Element>
  );
}

export { BrandLogo, LogoMark };
